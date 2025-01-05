package views.web

import controllers.GameController
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import models.GameState
import models.Player
import models.tiles.Hand
import util.serializeGameState
import views.BoardController
import views.InputReader
import views.MessageOutput
import views.text.TextIn
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Manages a single game instance including players, connections, and game state.
 * Handles communication between players and game logic.
 */
class GameRoom(
    val gameId: String,
    private val json: Json,
    private val maxPlayers: Int = 4
) : BoardController, InputReader, MessageOutput {

    // Thread-safe collections for managing game state
    private val connections = ConcurrentHashMap<String, Connection>()
    private val messageQueues = ConcurrentHashMap<String, Queue<String>>()
    private val inputQueues = ConcurrentHashMap<String, Queue<String>>()
    private lateinit var gameController: GameController
    var isGameStarted = false

    /**
     * Attempts to add a new player to the game.
     * @return true if player was added successfully, false if game is full or started
     */
    suspend fun addPlayer(connectionId: String, playerName: String, session: WebSocketSession): Boolean {
        if (isGameStarted || connections.size >= maxPlayers) {
            return false
        }

        val connection = Connection(connectionId, session, playerName)
        connections[connectionId] = connection
        messageQueues[playerName] = ConcurrentLinkedQueue()
        inputQueues[playerName] = ConcurrentLinkedQueue()

        broadcastGameState()
        announcePlayerJoined(playerName)
        return true
    }

    /**
     * Attempts to start the game if enough players have joined.
     *
     * @return true if game was started successfully
     */
    fun startGame(): Boolean {
        if (isGameStarted || connections.size < 1) return false
        gameController = GameController()
        gameController.startGame(connections.size, this)
        isGameStarted = true
        broadcastGameState()
        return true
    }

    /**
     * Broadcasts current game state to all connected players.
     */
    private fun broadcastGameState() {
        val gameState = WebSocketMessage.GameState(
            gameId = gameId,
            players = connections.values.map { it.playerName },
            isStarted = isGameStarted
        )
        broadcastMessage(gameState)
    }

    /**
     * Broadcasts a message to specified players or all players if none specified.
     */
    private fun broadcastMessage(
        message: WebSocketMessage,
        targetPlayers: List<String>? = null
    ) {
        connections.values.forEach { connection ->
            if (targetPlayers == null || targetPlayers.contains(connection.playerName)) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        connection.session.send(Frame.Text(json.encodeToString(WebSocketMessage.serializer(), message)))
                    } catch (e: Exception) {
                        println("Error broadcasting message to ${connection.playerName}: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Announces when a new player joins the game.
     */
    private fun announcePlayerJoined(playerName: String) {
        val joinMessage = WebSocketMessage.GameMessage(
            player = "System",
            content = "$playerName has joined the game"
        )
        broadcastMessage(joinMessage)
    }

    // BoardOutput interface implementation
    override fun push(game: GameState) {
        connections.values.forEach { connection ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    connection.session.send(Frame.Text(serializeGameState(game, connection.playerName)))
                } catch (e: Exception) {
                    println("Error sending game state to ${connection.playerName}: ${e.message}")
                }
            }
        }
    }

    // Player management methods
    override fun getPlayers(): List<Player> {
        return connections.values.map { connection ->
            Player(
                connection.playerName,
                TextIn(this, this),
                Hand(listOf())
            )
        }
    }

    override fun waitForPlayers(playerCount: Int) {
        while (connections.size < playerCount) {
            Thread.sleep(1000)
        }
    }

    // Input/Output handling
    override fun getNextInput(player: String): String {
        while (true) {
            inputQueues[player]?.poll()?.let { return it }
            Thread.sleep(100)
        }
    }

    override fun showMessage(message: String, player: String) {
        val systemMessage = WebSocketMessage.GameMessage(
            player = "System",
            content = message
        )
        broadcastMessage(systemMessage, listOf(player))
    }

    /**
     * Processes incoming WebSocket messages.
     */
    suspend fun handleMessage(connectionId: String, message: WebSocketMessage) {
        when (message) {
            is WebSocketMessage.GameMessage -> {
                val playerName = connections[connectionId]?.playerName
                messageQueues[playerName]?.add(message.content)
            }

            is WebSocketMessage.GameInput -> {
                val playerName = connections[connectionId]?.playerName
                inputQueues[playerName]?.add(message.content)
            }

            else -> println("Unexpected message type in game room")
        }
    }

    /**
     * Handles player disconnection.
     */
    suspend fun removePlayer(connectionId: String) {
        val connection = connections.remove(connectionId)
        if (connection != null) {
            messageQueues.remove(connection.playerName)
            inputQueues.remove(connection.playerName)
            announcePlayerLeft(connection.playerName)
            broadcastGameState()
        }
    }

    private fun announcePlayerLeft(playerName: String) {
        val leaveMessage = WebSocketMessage.GameMessage(
            player = "System",
            content = "$playerName has left the game"
        )
        broadcastMessage(leaveMessage)
    }
}