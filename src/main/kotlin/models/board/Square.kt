package models.board

import models.Player
import models.tiles.Piece

data class Square(
    val multiplier: Multiplier,
    val piece: Piece? = null,
    val turnPlaced: Int? = null,
    val playerPlaced: Player? = null
) {
    fun hasPiece(): Boolean = piece != null

    fun withPiece(piece: Piece, turnPlaced: Int?, playerPlaced: Player?): Square {
        return Square(multiplier, piece, turnPlaced, playerPlaced)
    }
}
