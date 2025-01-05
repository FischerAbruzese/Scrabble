package views

import models.GameState
import models.Player

/**
 * Displays the board gui and gets info for building game
 */
interface BoardController {
    fun push(game: GameState)

    fun waitForPlayers(playerCount: Int) {

    }

    fun getPlayers(): List<Player>

    suspend fun closeAllConnections() {

    }
}