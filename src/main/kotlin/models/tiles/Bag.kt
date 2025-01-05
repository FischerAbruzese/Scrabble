package models.tiles

import exceptions.NotEnoughPiecesException

class Bag(pieces: List<Piece>) {
    val pieces: MutableList<Piece> = pieces.shuffled().toMutableList()

    fun isEmpty(): Boolean {
        return size() == 0
    }

    /**
     * Number of pieces left in the bag
     */
    fun size(): Int {
        return pieces.size
    }

    /**
     * Gets and removes a random piece from the bag. Null if the bag is empty.
     */
    fun nextPiece(): Piece? {
        return pieces.removeLastOrNull()
    }

    fun draw(n: Int): List<Piece> {
        var pickSize = n
        if (pickSize > size()) pickSize = size()
        return List(pickSize) { nextPiece()!! }
    }

    /**
     * Returns but does not remove the next piece in the bag that will be returned by [nextPiece] if no exchanges are made. Null if the bag is empty.
     */
    fun peekNextPiece(): Piece? {
        return pieces.lastOrNull()
    }

    /**
     * Draws a number of pieces from the bag equal to the number of exchanges and puts all the exchanges in the bag.
     */
    @Throws(NotEnoughPiecesException::class)
    fun exchange(exchanges: Collection<Piece>): List<Piece> {
        if (pieces.size > size()) throw NotEnoughPiecesException("Bag does not have enough pieces to exchange")
        val drawn = List(exchanges.size) { nextPiece()!! }
        pieces.addAll(exchanges)
        pieces.shuffle()
        return drawn
    }

    override fun toString(): String {
        return "${pieces.size} pieces left"
    }
}