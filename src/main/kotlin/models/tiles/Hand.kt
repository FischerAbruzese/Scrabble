package models.tiles

import exceptions.NotEnoughPiecesException
import kotlin.jvm.Throws

class Hand(piecesInit: List<Piece>) {
    var pieces = piecesInit.toMutableList()

    fun classInv(): Boolean = pieces.size in 0..7

    fun size() = pieces.size

//    fun addAll(piecesToAdd: List<Piece>) = pieces.addAll(piecesToAdd)

//    /**
//     * Removes a list of pieces from the hand if all the pieces are in the hand
//     *
//     * @return true if all pieces were removed, false if nothing was done
//     */
//    @JvmName("removePieces1")
//    fun removePieces(piecesToRemove: List<Piece>): Boolean {
//        if (!containsPieces(piecesToRemove)) return false
//        pieces.removeAll(piecesToRemove)
//        return true
//    }

    /**
     * Checks if every element of a list of pieces is in the hand
     *
     * @return List of matched pieces
     */
    @JvmName("containsPieces1")
    fun containsPieces(piecesToCheck: List<Piece>): List<Piece>?{
        if(pieces.containsAll(piecesToCheck))
            return piecesToCheck
        return null
    }

//    /**
//     * Removes a list of pieces from the hand if all the pieces are in the hand
//     *
//     * @return true if all pieces were removed, false if nothing was done
//     */
//    @JvmName("removePieces2")
//    fun removePieces(piecesToRemove: List<Char>): Boolean {
//        val matchChars = matchChars(piecesToRemove) ?: return false
//        pieces = matchChars.second.toMutableList()
//        return true
//    }

    /**
     * Checks if every element of a list of pieces is in the hand
     *
     * @return List of matched pieces
     */
    @JvmName("containsPieces2")
    fun containsPieces(piecesToCheck: List<Char>): List<Piece>? {
        return matchChars(piecesToCheck)?.first
    }

    /**
     * Given a list of characters, attempts to find a separate out a matching list of pieces
     *
     * @return Pair of matched pieces and the remainder pieces
     */
    fun matchChars(piecesToMatch: List<Char>): Pair<List<Piece>, List<Piece>>? {
        val matched = ArrayList<Piece>()
        val piecesCopy = pieces.toMutableList()
        outer@ for (m in piecesToMatch) {
            for (p in piecesCopy) {
                if (p.letter == m) {
                    matched.add(p)
                    piecesCopy.remove(p)
                    continue@outer
                }
            }
            return null // No match
        }
        return Pair(matched, piecesCopy)
    }

    fun isEmpty(): Boolean = pieces.isEmpty()

    /**
     * Exchanges a list of pieces from the bag
     *
     * Requires all of [piecesToExchange] to be in the bag
     *
     * @return List of pieces that were pulled
     */
    @Throws(NotEnoughPiecesException::class)
    fun exchangePieces(bag: Bag, piecesToExchange: List<Piece>): List<Piece>{
        val pulled = bag.exchange(piecesToExchange)
        pieces.removeAll(piecesToExchange)
        pieces.addAll(pulled)
        return pulled
    }

    /**
     * Replaces a list of pieces with pieces from the bag
     *
     * Requires all of [piecesToUse] to be in the hand
     *
     * @return List of pieces that were pulled
     */
    fun usePieces(bag: Bag, piecesToUse: List<Piece>): List<Piece> {
        pieces.removeAll(piecesToUse)
        val pulled = bag.draw(piecesToUse.size)
        pieces.addAll(pulled)
        return pulled
    }
}