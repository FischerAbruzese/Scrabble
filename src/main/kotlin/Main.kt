import controllers.GameController
import controllers.players.AI2
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
    val humanController = ConsolePlayerController.INSTANCE
    val human = Player("sky", humanController)
    val ai = Player("AI", aiController)
    GameController().startGame(
        ConsoleBoard(
            listOf(
                human,
                ai
            )
        )
    )
}