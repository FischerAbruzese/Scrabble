package views.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class WebSocketMessage {
    @Serializable
    @SerialName("JOIN")
    data class JoinMessage(
        val type: String = "JOIN",
        val name: String
    ) : WebSocketMessage()

    @Serializable
    @SerialName("MESSAGE")
    data class GameMessage(
        val type: String = "MESSAGE",
        val player: String,
        val content: String
    ) : WebSocketMessage()

    @Serializable
    @SerialName("INPUT")
    data class GameInput(
        val type: String = "INPUT",
        val player: String,
        val content: String
    ) : WebSocketMessage()
}