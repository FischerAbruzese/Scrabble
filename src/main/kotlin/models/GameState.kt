package models

import models.board.Board
import models.tiles.Bag
import models.turn.Exchange
import models.turn.Move
import models.turn.Pass
import models.turn.Turn


class GameState(
    val players: List<Player>,
    val board: Board = Board(),
    val bag: Bag = Bag(),
    /**
     * Number of turns that have been made so far
     */
    var turnNum: Int = 0,

    /**
     * Number of players who have passed in a row so far. When equal to the number of players, the game is over.
     */
    var passStreak: Int = 0
) {
    /**
     * Executes a turn if valid
     *
     * @return true if the turn ends the game, false otherwise
     */
    fun makeTurn(turn: Turn): Boolean {
        val nextPlayer = currentPlayer()
        when (turn) {
            is Move -> {
                if (!isValidMove(turn)) throw InvalidMoveException()
                passStreak = 0

                return checkEnd()
            }

            is Exchange -> {
                passStreak = 0
                return false //Exchanging can never end the game
            }

            is Pass -> {
                passStreak++
                return checkEnd()
                //Do Nothing
            }

            else -> throw IllegalArgumentException("Unimplemented turn type")
        }
        turnNum++
        return true
    }

    /**
     * The player whose turn it is to move
     */
    fun currentPlayer() = players[turnNum % players.size]


    /**
     * Checks if the game has ended and performs post-game actions
     */
    fun checkEnd(): Boolean {
        if (passStreak < players.size) return false
        players.forEach { it.gameEnd() }
        return true
    }
}