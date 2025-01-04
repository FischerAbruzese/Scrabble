package views

import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.html.*
import models.GameState
import java.util.concurrent.atomic.AtomicInteger

class WebOut : ViewOutput {
    private val pushCount = AtomicInteger(0)
    private val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title { +"Scrabble Game" }
                        style {
                            +"""
                                body {
                                    font-family: Arial, sans-serif;
                                    display: flex;
                                    justify-content: center;
                                    align-items: center;
                                    height: 100vh;
                                    margin: 0;
                                    background-color: #f0f0f0;
                                }
                                .container {
                                    background-color: white;
                                    padding: 20px;
                                    border-radius: 8px;
                                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                                }
                            """
                        }
                    }
                    body {
                        div(classes = "container") {
                            h1 { +"Scrabble Game State" }
                            p { +"Number of updates received: ${pushCount.get()}" }
                        }
                    }
                }
            }
        }
    }

    init {
        server.start()
        println("Web interface started at http://localhost:8080")
    }

    override fun push(game: GameState) {
        pushCount.incrementAndGet()
    }
}