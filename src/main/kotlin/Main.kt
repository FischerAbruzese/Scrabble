import controllers.GameController
import models.Player
import views.text.ConsoleBoard
import views.text.ConsolePlayerController
import views.web.GameLobby



fun main(args: Array<String>) {
    when {
        args.contains("--web") -> {
            val config = ServerConfig.load()
            val isProduction = args.contains("--prod")

            startWebGame(config.copy(isProduction = isProduction))
        }
        else -> startTextGame()
    }
}

fun startWebGame(config: ServerConfig) {
    val host = if (config.isProduction) config.productionUrl else config.host
    println("Starting server on $host:${config.port}")

    val lobby = GameLobby(
        port = config.port,
        host = host
    )

    // The following line will block until the server stops
    Thread.currentThread().join()
}

fun startTextGame() {
    val sky = Player("Sky", ConsolePlayerController.INSTANCE)
    val mari = Player("Mari", ConsolePlayerController.INSTANCE)
    GameController().startGame(
        ConsoleBoard(
            listOf(
                //players
                sky,
                mari
            )
        )
    )
}