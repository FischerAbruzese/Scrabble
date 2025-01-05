import controllers.GameController
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
    val sky = Player("Sky", ConsolePlayerController.INSTANCE)
    val mari = Player("Mari", ConsolePlayerController.INSTANCE)
    GameController().startGame(
        2,
        ConsoleBoard(
            listOf(
                //players
                sky,
                mari
            )
        )
    )
}