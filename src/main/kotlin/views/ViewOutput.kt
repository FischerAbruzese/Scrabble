package views

import models.GameState

interface ViewOutput {
    fun push(game: GameState)

    fun waitForPlayers(playerCount: Int) {

    }
}