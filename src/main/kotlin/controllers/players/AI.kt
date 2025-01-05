package controllers.players

import models.GameState
import models.Player
import models.turn.Direction
import models.turn.Move
import models.turn.Turn

class AI : PlayerController {
    //AI Parameters
    /**
     * Minimum score of a move to be considered. Must be greater than 0
     */
    private val moveScoreCutoff = 3



    override fun getTurn(gameState: GameState, player: Player): Turn {
        val (bestMove: Move?, bestMoveScore: Int) = maxOf(
            bestMove(gameState, player, Direction.ACROSS),
            bestMove(gameState, player, Direction.DOWN),
            comparator = compareBy{it.second}
        )

        if(bestMoveScore > moveScoreCutoff && bestMove != null) return bestMove



    }

    /**
     * Finds the best move in the given direction for the given player
     *
     * @return the best move and its score
     */
    private fun bestMove(gameState: GameState, player: Player, direction: Direction): Pair<Move?, Int> {
        var bestMoveScore = -1
        for(emptySquare in gameState.board.filter { !it.hasPiece() }){

        }
    }
}