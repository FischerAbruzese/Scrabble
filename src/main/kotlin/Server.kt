import controllers.GameController

class Server {
    fun startServer() {
        while (true) {
            val gameController = GameController()
            gameController.startGame()
        }
    }
}