package models.board

import models.Player
import models.tiles.Piece

class Square(
    val multiplier: Multiplier,
    var piece: Piece? = null,
    val turnPlaced: Int? = null,
    val playerPlaced: Player? = null
) {
    fun hasPiece(): Boolean = piece != null
}
