package views.web

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import views.web.models.WebSocketMessage
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

/**
 * GameLobby manages the game lobby system, handling:
 * - Player connections
 * - Game creation and management
 * - WebSocket communication
 * - Game state updates
 */
class GameLobby(
    private val port: Int = 8080,
    private val host: String = "localhost",
    private val maxPlayersPerGame: Int = 4
) {
    // JSON serializer configuration
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    // Thread-safe map of active games
    private val activeGames = ConcurrentHashMap<String, GameRoom>()

    // Ktor server instance
    private val server = embeddedServer(Netty, port = port) {
        configureWebSocket()
        configureCORS()
        configureRouting()
    }

    /**
     * Configures WebSocket settings for the server
     */
    private fun Application.configureWebSocket() {
        install(WebSockets) {
            pingPeriod = Duration.parse("15s")
            timeout = Duration.parse("15s")
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
    }

    /**
     * Configures CORS settings for the server
     */
    private fun Application.configureCORS() {
        install(CORS) {
            anyHost()
            allowHeader("Content-Type")
        }
    }

    /**
     * Configures routing for both lobby and game endpoints
     */
    private fun Application.configureRouting() {
        routing {
            // Lobby endpoint
            webSocket("/lobby") {
                handleLobbySession(this)
            }

            // Game room endpoint
            webSocket("/game/{id}") {
                handleGameSession()
            }
        }
    }

    /**
     * Handles an individual lobby WebSocket session
     */
    private suspend fun handleLobbySession(session: WebSocketSession) {
        val connectionId = UUID.randomUUID().toString()
        println("Client connected to lobby: $connectionId")

        try {
            for (frame in session.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        handleLobbyMessage(session, frame.readText())
                    }

                    else -> { /* Ignore other frame types */
                    }
                }
            }
        } catch (e: Exception) {
            println("Error in lobby session: ${e.message}")
        }
    }

    /**
     * Handles an individual game room WebSocket session
     */
    private suspend fun DefaultWebSocketServerSession.handleGameSession() {
        val gameId = call.parameters["id"] ?: run {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Game ID required"))
            return
        }

        val gameRoom = activeGames[gameId] ?: run {
            this.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Game not found"))
            return
        }

        val connectionId = UUID.randomUUID().toString()
        println("Client connected to game $gameId: $connectionId")

        try {
            for (frame in this.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        handleGameMessage(gameRoom, connectionId, frame.readText())
                    }

                    else -> { /* Ignore other frame types */
                    }
                }
            }
        } catch (e: Exception) {
            println("Error in game session: ${e.message}")
        } finally {
            gameRoom.removePlayer(connectionId)
            cleanupInactiveGames()
        }
    }

    /**
     * Processes incoming lobby messages
     */
    private suspend fun handleLobbyMessage(session: WebSocketSession, message: String) {
        try {
            when (val parsed = json.decodeFromString<WebSocketMessage>(message)) {
                is WebSocketMessage.CreateGameMessage -> {
                    createNewGame(session, parsed)
                }

                is WebSocketMessage.JoinGameMessage -> {
                    joinExistingGame(session, parsed)
                }

                is WebSocketMessage.StartGameMessage -> {
                    startGame(parsed.gameId)
                }

                else -> {
                    sendErrorMessage(session, "Unexpected message type in lobby")
                }
            }
        } catch (e: Exception) {
            sendErrorMessage(session, "Error processing message: ${e.message}")
        }
    }

    /**
     * Creates a new game room
     */
    private suspend fun createNewGame(session: WebSocketSession, message: WebSocketMessage.CreateGameMessage) {
        val gameId = UUID.randomUUID().toString()
        val gameRoom = GameRoom(gameId, json, maxPlayersPerGame)
        activeGames[gameId] = gameRoom

        // Notify creator of successful game creation
        session.send(
            Frame.Text(
                json.encodeToString(
                    WebSocketMessage.GameState.serializer(),
                    WebSocketMessage.GameState(
                        gameId = gameId,
                        players = listOf(message.name),
                        isStarted = false
                    )
                )
            )
        )

        println("New game created: $gameId by ${message.name}")
    }

    /**
     * Handles a player joining an existing game
     */
    private suspend fun joinExistingGame(session: WebSocketSession, message: WebSocketMessage.JoinGameMessage) {
        val gameRoom = activeGames[message.gameId] ?: run {
            sendErrorMessage(session, "Game not found")
            return
        }

        val success = gameRoom.addPlayer(
            UUID.randomUUID().toString(),
            message.name,
            session
        )

        if (!success) {
            sendErrorMessage(session, "Cannot join game - Game is either full or already started")
        }
    }

    /**
     * Starts a game if possible
     */
    private fun startGame(gameId: String) {
        activeGames[gameId]?.let { gameRoom ->
            if (gameRoom.startGame()) {
                println("Game started: $gameId")
            } else {
                println("Failed to start game: $gameId")
            }
        }
    }

    /**
     * Processes incoming game messages
     */
    private suspend fun handleGameMessage(gameRoom: GameRoom, connectionId: String, message: String) {
        try {
            val parsed = json.decodeFromString<WebSocketMessage>(message)
            gameRoom.handleMessage(connectionId, parsed)
        } catch (e: Exception) {
            println("Error processing game message: ${e.message}")
        }
    }

    /**
     * Sends an error message to a client
     */
    private suspend fun sendErrorMessage(session: WebSocketSession, content: String) {
        try {
            session.send(
                Frame.Text(
                    json.encodeToString(
                        WebSocketMessage.GameMessage.serializer(),
                        WebSocketMessage.GameMessage(
                            player = "System",
                            content = content
                        )
                    )
                )
            )
        } catch (e: Exception) {
            println("Error sending error message: ${e.message}")
        }
    }

    /**
     * Removes inactive games from the lobby
     */
    private fun cleanupInactiveGames() {
        activeGames.entries.removeIf { (gameId, game) ->
            val isEmpty = game.getPlayers().isEmpty()
            if (isEmpty) {
                println("Removing inactive game: $gameId")
            }
            isEmpty
        }
    }

    /**
     * Gets a list of available (not started) games
     */
    fun getAvailableGames(): List<String> {
        return activeGames.entries
            .filter { !it.value.isGameStarted }
            .map { it.key }
    }

    /**
     * Gets the count of all active games
     */
    fun getActiveGameCount(): Int = activeGames.size

    /**
     * Checks if a specific game exists and is available
     */
    fun isGameAvailable(gameId: String): Boolean {
        return activeGames[gameId]?.let { !it.isGameStarted } ?: false
    }

    /**
     * Gets the number of players in a specific game
     */
    fun getGamePlayerCount(gameId: String): Int {
        return activeGames[gameId]?.getPlayers()?.size ?: 0
    }

    /**
     * Schedules periodic cleanup of inactive games
     */
    private fun startCleanupScheduler() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                cleanupInactiveGames()
                kotlinx.coroutines.delay(5 * 60 * 1000) // Run every 5 minutes
            }
        }
    }

    /**
     * Starts the server
     */
    fun start() {
        server.start(wait = false)
        startCleanupScheduler()
        println("Game Lobby started:")
        println("- Lobby WebSocket endpoint: ws://$host:$port/lobby")
        println("- Game WebSocket endpoint: ws://$host:$port/game/{id}")
    }

    /**
     * Stops the server and cleans up resources
     */
    fun shutdown() {
        server.stop(1000, 2000)
        activeGames.clear()
        println("Game Lobby shutdown complete")
    }

    // Start server on initialization
    init {
        start()
    }
}