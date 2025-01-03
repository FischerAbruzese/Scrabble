package models.board

import models.tiles.Piece

class Square(
    val multiplier: Multiplier,
    var piece: Piece? = null,
    val turnPlaced: Int? = null,
    val playerPlaced: Int? = null
) {
    fun hasPiece(): Boolean = piece != null
}
