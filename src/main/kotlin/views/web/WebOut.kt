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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import models.GameState
import models.Player
import models.tiles.Hand
import util.serializeGameState
import views.ViewOutput
import views.text.TextIn
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

@Serializable
data class JoinMessage(
    val type: String,
    val name: String
)

class WebOut : ViewOutput {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val playerNames = ConcurrentHashMap<String, String>() // Maps connectionId to player name

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val server = embeddedServer(Netty, port = 8080) {
        install(WebSockets) {
            pingPeriod = Duration.parse("15s")
            timeout = Duration.parse("15s")
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        install(CORS) {
            anyHost()
            allowHeader("Content-Type")
        }

        routing {
            webSocket("/game-state") {
                val connectionId = UUID.randomUUID().toString()
                println("Client connected: $connectionId")

                try {
                    connections[connectionId] = this

                    var nameReceived = false
                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val receivedText = frame.readText()
                                    try {
                                        val message = json.decodeFromString<JoinMessage>(receivedText)
                                        if (message.type == "JOIN" && !nameReceived) {
                                            playerNames[connectionId] = message.name
                                            nameReceived = true
                                            println("Player ${message.name} joined with connection $connectionId")
                                        }
                                    } catch (e: Exception) {
                                        println("Error processing message from $connectionId: ${e.message}")
                                    }
                                }

                                else -> { /* Ignore other frame types */
                                }
                            }
                        }
                    } finally {
                        connections.remove(connectionId)
                        playerNames.remove(connectionId)
                        println("Client disconnected: $connectionId")
                    }
                } catch (e: Exception) {
                    println("Error in WebSocket connection: ${e.message}")
                    connections.remove(connectionId)
                    playerNames.remove(connectionId)
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
        val gameStateJson = serializeGameState(game)

        connections.forEach { (id, session) ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    session.send(Frame.Text(gameStateJson))
                    println("Sent update to client: $id")
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

    fun getPlayers(): List<Player> {
        return playerNames.values.map { name ->
            Player(
                name,
                TextIn(),
                Hand(listOf())
            )
        }
    }
}