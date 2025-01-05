package views.web

import io.ktor.websocket.*

/**
 * Represents a WebSocket connection with associated player information.
 * @property connectionId Unique identifier for the connection
 * @property session WebSocket session
 * @property playerName Name of the connected player
 */
data class Connection(
    val connectionId: String,
    val session: WebSocketSession,
    var playerName: String
)