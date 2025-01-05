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
    @SerialName("CREATE_GAME")
    data class CreateGameMessage(
        val type: String = "CREATE_GAME",
        val name: String
    ) : WebSocketMessage()

    @Serializable
    @SerialName("JOIN_GAME")
    data class JoinGameMessage(
        val type: String = "JOIN_GAME",
        val gameId: String,
        val name: String
    ) : WebSocketMessage()

    @Serializable
    @SerialName("START_GAME")
    data class StartGameMessage(
        val type: String = "START_GAME",
        val gameId: String
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

    @Serializable
    @SerialName("GAME_STATE")
    data class GameState(
        val type: String = "GAME_STATE",
        val gameId: String,
        val players: List<String>,
        val isStarted: Boolean
    ) : WebSocketMessage()
}
