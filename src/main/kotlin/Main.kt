import controllers.GameController
import main.kotlin.controllers.players.AI2
import models.Player
import views.text.ConsoleBoard
import views.text.ConsolePlayerController
import views.web.GameLobby

fun main() {
    startTextGame()
}

fun startWebGame() {
    val lobby = GameLobby()
    // The following line will block until the server stops
    Thread.currentThread().join()
}

fun startTextGame() {
    val aiController = AI2(1000)
    val ai1 = Player("AI 1", aiController)
    val ai2 = Player("AI 2", aiController)
    GameController().startGame(
        ConsoleBoard(
            listOf(
                ai1,
                ai2
            )
        )
    )
}