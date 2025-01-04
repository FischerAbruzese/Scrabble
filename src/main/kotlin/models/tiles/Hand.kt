package models.tiles

class Hand(var pieces: MutableList<Piece>) {
    fun classInv(): Boolean = pieces.size in 0..7

    fun size() = pieces.size

    /**
     * Removes a list of pieces from the hand if all the pieces are in the hand, otherwise returns false
     */
    fun removePieces(piecesToRemove: List<Piece>): Boolean{
        if(!containsPieces(piecesToRemove)) return false
        pieces.removeAll(piecesToRemove)
        return true
    }

    /**
     * Checks if every element of a list of pieces is in the hand
     */
    fun containsPieces(piecesToCheck: List<Piece>): Boolean = pieces.containsAll(piecesToCheck)

    /**
     * Removes a list of pieces from the hand if all the pieces are in the hand, otherwise returns false
     */
    fun removePieces(piecesToRemove: List<Char>): Boolean{
        if(!containsPieces(piecesToRemove)) return false
        for(pieceToRemove in piecesToRemove){
            for(piece in pieces){
                if(piece.letter == pieceToRemove){
                    pieces.remove(piece)
                    break
                }
            }
        }
        return true
    }

    /**
     * Checks if every element of a list of pieces is in the hand
     */
    fun containsPieces(piecesToCheck: List<Char>): Boolean{
        val check: MutableList<Char> = pieces.map{it.letter}.toMutableList()
        for(piece in piecesToCheck){
            if (!check.remove(piece)) return false
        }
        return true
    }
}