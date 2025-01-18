import controllers.GameController
import controllers.players.Ai
import models.Player
import views.text.ConsoleBoard
import views.text.ConsolePlayerController
import views.web.GameLobby
import java.io.Console
import kotlin.random.Random

fun main(args: Array<String>) {
    val playerList = mutableListOf<Player>()
    var currentPlayer: Player? = null;
    //check what players to add
    for((i,arg) in args.withIndex()){
        //add currentPlayer and create new currentPlayer
        if(arg[0] == '-'){
            if(currentPlayer != null) {
                playerList.add(currentPlayer)
            }
            currentPlayer = when(arg.subSequence(1, arg.length)){
                "human" -> Player("Human $i", ConsolePlayerController.INSTANCE)
                "ai" -> Player("Ai $i", Ai(0))
                else -> throw IllegalArgumentException("Unknown player type: ${arg.subSequence(1, arg.length)}")
            }
        }
        //update name of currentPlayer
        else {
            if(currentPlayer == null) throw IllegalArgumentException("No player type specified for $arg")
            currentPlayer = Player(arg, currentPlayer.playerController)
        }
    }
    //Default game
    currentPlayer ?: playerList.addAll(listOf(
        Player("Ai 1", Ai(0)),
        Player("Ai 2", Ai(0))
    ))
    if(currentPlayer != null) playerList.add(currentPlayer)

    GameController().startGame(ConsoleBoard(playerList))
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
    val seed = Random(69).nextInt(1000000)
    println("Seed: $seed")
    GameController(Random(69)).startGame(
        ConsoleBoard(
            listOf(
                human,
                ai
            )
        )
    )
}