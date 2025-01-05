package models.turn

import models.tiles.Piece

data class Exchange(
    /**
     * Pieces to be traded in
     */
    val exchangePieces: List<Piece>
) : Turn {

    fun classInv(): Boolean {
        return exchangePieces.size in 1..7
    }
}