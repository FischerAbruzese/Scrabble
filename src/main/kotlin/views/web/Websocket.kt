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
import models.GameState
import models.Player
import models.tiles.Hand
import util.serializeGameState
import views.BoardOutput
import views.InputReader
import views.MessageOutput
import views.text.TextIn
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration

class WebOut : BoardOutput, InputReader, MessageOutput {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val playerNames = ConcurrentHashMap<String, String>() // Maps connectionId to player name
    private val messageQueue = ConcurrentHashMap<String, Queue<String>>()
    private val inputQueue = ConcurrentHashMap<String, Queue<String>>()

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val server = embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
        install(WebSockets) {
            pingPeriod = Duration.parse("15s")
            timeout = Duration.parse("15s")
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        install(CORS) {
            allowHost("scrabble-j2qi.onrender.com", schemes = listOf("https"))
            allowHeader("Content-Type")
        }


        routing {
            webSocket("/game-state") {
                val connectionId = UUID.randomUUID().toString()
                println("Client connected: $connectionId")

                try {
                    connections[connectionId] = this

                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                try {
                                    when (val message = json.decodeFromString<WebSocketMessage>(receivedText)) {
                                        is WebSocketMessage.JoinMessage -> {
                                            playerNames[connectionId] = message.name
                                            messageQueue[message.name] = ConcurrentLinkedQueue()
                                            inputQueue[message.name] = ConcurrentLinkedQueue()
                                            println("Player ${message.name} joined with connection $connectionId")

                                            // Broadcast join message to all clients
                                            val joinAnnouncement = WebSocketMessage.GameMessage(
                                                player = "System",
                                                content = "${message.name} has joined the game"
                                            )
                                            broadcastMessage(joinAnnouncement)
                                        }

                                        is WebSocketMessage.GameMessage -> {
                                            // Store message in queue for the recipient
                                            messageQueue[message.player]?.add(message.content)
                                            // Broadcast message to all clients
                                            broadcastMessage(message)
                                        }

                                        is WebSocketMessage.GameInput -> {
                                            // Add input to queue for processing
                                            inputQueue[message.player]?.add(message.content)
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Error processing message from $connectionId: ${e.message}")
                                }
                            }

                            else -> { /* Ignore other frame types */
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error in WebSocket connection: ${e.message}")
                } finally {
                    // Clean up when connection closes
                    connections.remove(connectionId)
                    val playerName = playerNames[connectionId]
                    if (playerName != null) {
                        messageQueue.remove(playerName)
                        inputQueue.remove(playerName)
                        playerNames.remove(connectionId)

                        // Broadcast leave message
                        val leaveMessage = WebSocketMessage.GameMessage(
                            player = "System",
                            content = "$playerName has left the game"
                        )
                        broadcastMessage(leaveMessage)
                    }
                }
            }
        }
    }

    private fun broadcastMessage(message: WebSocketMessage.GameMessage) {
        connections.forEach { (_, session) ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    session.send(Frame.Text(json.encodeToString(WebSocketMessage.GameMessage.serializer(), message)))
                } catch (e: Exception) {
                    println("Error broadcasting message: ${e.message}")
                }
            }
        }
    }

    init {
        server.start(wait = false)
        println("Server started - WebSocket endpoint at ws://localhost:8080/game-state")
    }

    override fun push(game: GameState) {
        println("Pushing update to ${connections.size} clients")
        connections.forEach { (id, session) ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    session.send(Frame.Text(serializeGameState(game, playerNames[id]!!)))
                    println("Sent update to client: ${playerNames[id]} ($id)")
                } catch (e: Exception) {
                    println("Error sending game state to $id: ${e.message}")
                }
            }
        }
    }

    override fun waitForPlayers(playerCount: Int) {
        while (playerNames.size < playerCount) {
            Thread.sleep(1000)
        }
    }

    override fun getPlayers(): List<Player> {
        return playerNames.values.map { name ->
            Player(
                name,
                TextIn(this, this),
                Hand(listOf())
            )
        }
    }

    override fun getNextInput(player: String): String {
        while (true) {
            inputQueue[player]?.poll()?.let { return it }
            Thread.sleep(100)
        }
    }

    override fun showMessage(message: String) {
        val systemMessage = WebSocketMessage.GameMessage(
            player = "System",
            content = message
        )
        broadcastMessage(systemMessage)
    }

    override suspend fun closeAllConnections() {
        connections.forEach { (connectionId, session) ->
            try {
                session.close(CloseReason(CloseReason.Codes.NORMAL, "Server shutting down"))
                println("Closed connection: $connectionId")
            } catch (e: Exception) {
                println("Error closing connection $connectionId: ${e.message}")
            }
        }
        connections.clear()
        playerNames.clear()
        messageQueue.clear()
        inputQueue.clear()
    }

    fun getConnections(): Map<String, WebSocketSession> = connections.toMap()
    fun getPlayerNames(): Map<String, String> = playerNames.toMap()
}