package models.tiles

class Hand(piecesInit: List<Piece>) {
    var pieces = piecesInit.toMutableList()

    fun classInv(): Boolean = pieces.size in 0..7

    fun size() = pieces.size

    /**
     * Removes and returns a list of pieces from the hand if all the pieces are in the hand, otherwise null
     */
    @JvmName("removePieces1")
    fun removePieces(piecesToRemove: List<Piece>): List<Piece>? {
        if (!containsPieces(piecesToRemove)) return null
        pieces.removeAll(piecesToRemove)
        return piecesToRemove
    }

    /**
     * Checks if every element of a list of pieces is in the hand
     */
    @JvmName("containsPieces1")
    fun containsPieces(piecesToCheck: List<Piece>): Boolean = pieces.containsAll(piecesToCheck)

    /**
     * Removes and returns a list of pieces from the hand if all the pieces are in the hand, otherwise null
     */
    @JvmName("removePieces2")
    fun removePieces(piecesToRemove: List<Char>): List<Piece>? {
        val matchChars = matchChars(piecesToRemove)
        if (matchChars == null) return null
        pieces = matchChars.second.toMutableList()
        return matchChars.first
    }

    /**
     * Checks if every element of a list of pieces is in the hand
     */
    @JvmName("containsPieces2")
    fun containsPieces(piecesToCheck: List<Char>): Boolean {
        return matchChars(piecesToCheck) != null
    }

    /**
     * Given a list of characters, attempts to find a separate out a matching list of pieces
     *
     * @return Pair of matched pieces and the remainder pieces
     */
    fun matchChars(piecesToMatch: List<Char>): Pair<List<Piece>, List<Piece>>? {
        val matched = ArrayList<Piece>()
        val piecesCopy = pieces.toMutableList()
        for (m in piecesToMatch) {
            for (p in piecesCopy) {
                if (p.letter == m) {
                    matched.add(p)
                    piecesCopy.remove(p)
                    break
                }
            }
            return null // No match
        }
        return Pair(matched, piecesCopy)
    }

    fun isEmpty(): Boolean = pieces.isEmpty()
}