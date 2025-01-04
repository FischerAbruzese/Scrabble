package views

import models.GameState
import models.Player
import models.turn.Turn

interface ViewInput {
    fun getTurn(gameState: GameState, player: Player): Turn
}