package views.web

import controllers.util.serializeGameState
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
import views.ViewOutput
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class WebOut : ViewOutput {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
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
                    try {
                        for (frame in incoming) {
                            // Keep connection alive
                        }
                    } finally {
                        connections.remove(connectionId)
                        println("Client disconnected: $connectionId")
                    }
                } catch (e: Exception) {
                    println("Error in WebSocket connection: ${e.message}")
                    connections.remove(connectionId)
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
        while (connections.size < playerCount) {
            Thread.sleep(1000)
        }
    }
}