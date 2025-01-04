package models.tiles

class Hand(val pieces: List<Piece>) {
    fun classInv(): Boolean = pieces.size in 0..7

    fun size() = pieces.size

    fun isEmpty(): Boolean = pieces.isEmpty()
}