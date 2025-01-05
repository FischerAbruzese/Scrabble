package views

import models.GameState
import models.Player

interface BoardController {
    fun push(game: GameState)

    fun waitForPlayers(playerCount: Int) {

    }

    fun getPlayers(): List<Player>

    suspend fun closeAllConnections() {

    }
}