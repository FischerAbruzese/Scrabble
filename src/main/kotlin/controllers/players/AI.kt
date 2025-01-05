package controllers.players

import models.GameState
import models.Player
import models.turn.Direction
import models.turn.Move
import models.turn.Turn

class AI : PlayerController {
    override fun getTurn(gameState: GameState, player: Player): Turn {
        var bestMoveScore = 0
        var bestMove: Move? = null

    }

    /**
     * Finds the best move in the given direction for the given player
     *
     * @return the best move and its score
     */
    private fun bestMove(gameState: GameState, player: Player, direction: Direction): (Move, Int)? {
        for(emptySquare in gameState.board.filter { !it.hasPiece() }){

        }
    }
}