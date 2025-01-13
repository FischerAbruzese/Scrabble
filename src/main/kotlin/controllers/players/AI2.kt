package controllers.players

import models.GameState
import models.Player
import models.board.Board
import models.board.Coord
import models.board.Square
import models.tiles.Hand
import models.tiles.Piece
import models.turn.*
import util.Dictionary
import java.util.*
import kotlin.collections.HashSet

class AI2(private val moveDelayMilli: Long = 0) : PlayerController {
    val prefixes = HashSet<String>().apply {
        Dictionary.words.forEach {
            for(i in 1..it.length) add(it.substring(0, i))
        }
    }

    override fun pushMessage(message: String, player: String) {
    }

    override fun getTurn(gameState: GameState, player: Player): Turn {
        Thread.sleep(moveDelayMilli)

        val hand = player.hand
        val bestMove = gameState.board.rows().withIndex().maxOf { row -> bestMoveAtRow(gameState, hand, row) }

        if(bestMove.move != null) {return bestMove.move} //Move exists

        if (gameState.bag.isEmpty()) return Pass() //No Move, no pieces to exchange

        return Exchange(player.hand.pieces.subList(0, minOf(gameState.bag.size(), hand.size())).toList())
    }

    /**
     * Returns the best move out of all moves that start on a square in the given row
     *
     * @return the max of bestMoveAtSpot for each spot in both directions, handles blank tiles
     */
    private fun bestMoveAtRow(
        gameState: GameState,
        hand: Hand,
        row: IndexedValue<Array<Square>>
    ): MoveAndScore {
        var bestMove = MoveAndScore()

        //Handle blanks
        val blank = hand.pieces.find { '_' == it.letter } //finds the first occurrence
        if(blank != null) {
            return ('a'..'z').maxOf {//for blanks, try a hand with each possible letter
                blank.letter = it
                bestMoveAtRow(
                    gameState,
                    hand,
                    row
                )
            }
        }

        var maxPieces = 0 //Number of empty squares between current spot and right edge
        for((colNum, square) in row.value.withIndex().reversed()) {
            val coord = Coord(colNum, row.index)

            if(square.hasPiece()) continue

            maxPieces++

            //check both directions
            bestMove = maxOf(
                bestMove,
                bestMoveAtSpot(gameState.board, hand, Direction.DOWN, coord, maxPieces),
                bestMoveAtSpot(gameState.board, hand, Direction.ACROSS, coord, maxPieces, true)
            )
        }

        return bestMove
    }

    private fun bestMoveAtSpot(
        board: Board,
        hand: Hand,
        direction: Direction,
        coord: Coord,
        maxLength : Int,
        ignoreSingletons: Boolean = false
    ): MoveAndScore {
        var minLength = findMinLength(board, coord, direction, hand.size()) ?: return MoveAndScore() //room for improvement(think i've seen this one on a report card before)

        if(ignoreSingletons) minLength = maxOf(minLength, 2) //singletons might have been checked in other direction

        var bestMove = MoveAndScore()

        fun searchPermutations(prefix: List<Piece> = listOf(), remaining: List<Piece> = hand.pieces.toList()) {
            if(prefix.size >= maxLength) return
            //check if there's any valid words with this prefix
            if(!prefixes.contains(prefix.joinToString("") { it.letter.toString() })) return

            for(letter in remaining) {
                if(minLength < prefix.size) { //try and score it if it's long enough
                    val move = Move(coord, direction, prefix + letter)
                    try{
                        val score = board.findMove(move).second //this should be the most expensive call
                        if(score > bestMove.score) bestMove = MoveAndScore(move, score) //this accesses variable inside fun bestMoveAtSpot
                    } catch (_:Exception) {  }
                }
                searchPermutations(prefix + letter, remaining - letter)
            }
        }
        searchPermutations(prefix = board.findPrefix(coord, direction))

        return bestMove
    }

    /** @return minimum word size to be able to be legally placed, null if it's greater than handSize */
    private fun findMinLength(board: Board, loc: Coord, dir: Direction, numPieces: Int = 7): Int? {
        fun Coord.plusParallel(i: Int): Coord = if(dir == Direction.ACROSS) this.add(i, 0) else add(0, i)
        fun Coord.plusPerpendicular(i: Int): Coord = if(dir == Direction.ACROSS) this.add(0, i) else add(i, 0)

        var curr = loc
        //check behind
        if(board.getOrNull(curr.plusParallel(-1))?.hasPiece() == true) return 1

        for(i in 1..numPieces) {
            if(
                board.getOrNull(curr.plusParallel(1))?.hasPiece() == true ||
                board.getOrNull(curr.plusPerpendicular(-1))?.hasPiece() == true ||
                board.getOrNull(curr.plusPerpendicular(1))?.hasPiece() == true
            ) return i
            curr = curr.plusParallel(1)
        }
        return null
    }

    /**
     * List of pieces before [coord] opposite to [direction]
     *
     * Iterates backwards in the direction, finding the prefix from the pieces it finds before the coord
     */
    private fun Board.findPrefix(coord: Coord, direction: Direction): List<Piece> {
        fun Coord.plusParallel(i: Int): Coord = if(direction == Direction.ACROSS) add(i, 0) else add(0, i)

        var nextCoord = coord.plusParallel(-1)
        val prefix = LinkedList<Piece>()
        while (nextCoord.x >= 0 && nextCoord.y >= 0 && this[nextCoord].hasPiece()) {
            prefix.addFirst(this[nextCoord].piece!!)

            nextCoord = nextCoord.plusParallel(-1)
        }
        return prefix
    }

    /** Contains a move and score and allows for null moves (default with -1 score) */
    private data class MoveAndScore(val move: Move?, val score: Int): Comparable<MoveAndScore> {
        /** constructs null move and -1 score */ constructor() : this(null, -1)

        override fun compareTo(other: MoveAndScore) = this.score.compareTo(other.score)
    }
}