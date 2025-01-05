import views.web.GameLobby

fun main() {
    val lobby = GameLobby()
    // The following line will block until the server stops
    Thread.currentThread().join()
}