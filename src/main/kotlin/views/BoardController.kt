package views

import models.GameState
import models.Player

interface BoardController {
    fun push(game: GameState)

    fun waitForPlayers(playerCount: Int) {

    }

    fun getPlayers(): List<Player> {
        return listOf(
//            Player(
//                "Mari",
//                TextIn(),
//                Hand(listOf())
//            ),
//            Player(
//                "Sky",
//                TextIn(),
//                Hand(listOf())
//            )
        )
    }

    suspend fun closeAllConnections() {

    }
}