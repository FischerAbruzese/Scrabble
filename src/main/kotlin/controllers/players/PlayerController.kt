package controllers.players

import models.GameState
import models.Player
import models.turn.Turn

interface PlayerController {
    fun getTurn(gameState: GameState, player: Player): Turn
    fun pushMessage(message: String) //TODO: Move to personalized output
}