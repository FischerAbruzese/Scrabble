import controllers.GameController
import controllers.players.Ai
import models.Player
import views.text.ConsoleBoard
import views.text.ConsolePlayerController
import views.web.GameLobby
import kotlin.random.Random

fun main() {
    startTextGame()
}

fun startWebGame() {
    val lobby = GameLobby()
    // The following line will block until the server stops
    Thread.currentThread().join()
}

fun startTextGame() {
    val aiController = Ai(0)
    val humanController = ConsolePlayerController.INSTANCE
    val human = Player("AImari", aiController)
    val ai = Player("AIsky", aiController)
    GameController(Random(72)).startGame(
        ConsoleBoard(
            listOf(
                human,
                ai
            )
        )
    )
}