package models

import controllers.players.PlayerController
import models.tiles.Hand

data class Player(
    /** Name of the player */
    val name: String = "Unnamed",
    /** The score for this player */
    var score: Int = 0,
    /** The controller for this player */
    val playerController: PlayerController,
    /** The hand for this player */
    val hand: Hand
) {

    fun gameEnd() {
        score -= hand.size()
    }
}