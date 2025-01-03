package models.turn

import models.tiles.Piece

data class Exchange(
    /**
     * Pieces to be traded in
     */
    val exchange: List<Piece>
) : Turn {

    fun classInv(): Boolean {
        return exchange.size in 1..7
    }
}