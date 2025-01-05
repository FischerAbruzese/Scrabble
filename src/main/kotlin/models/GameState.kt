package models

import models.board.Board
import models.board.Coord
import models.tiles.Bag
import models.tiles.Piece


class GameState(
    val players: List<Player>,
    val bag: Bag,
    val board: Board = Board(),
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
     * The player whose turn it is to move
     */
    fun currentPlayer() = players[turnNum % players.size]

    fun gameOver(): Boolean {
        return players.any { it.isHandEmpty() } || passStreak == players.size
    }

    fun placePiece(coord: Coord, piece: Piece) {
        board[coord] = board[coord].withPiece(piece, turnNum, currentPlayer())
    }
}