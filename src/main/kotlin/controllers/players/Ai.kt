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

class Ai(private val moveDelayMilli: Long = 0) : PlayerController {
    val prefixes = HashSet<String>().apply {
        Dictionary.words.forEach {
            for(i in 1..it.length) add(it.substring(0, i))
        }
        add("")
    }

    override fun pushMessage(message: String, player: String) {
        //TODO remove this
        println("<$player> $message")
    }

    override fun getTurn(gameState: GameState, player: Player): Turn {
        Thread.sleep(moveDelayMilli)

        val hand = player.hand
        val bestMove = gameState.board.matrix
            .rows()
            .withIndex()
            .asSequence()
            .toList()
            .maxOf { row -> bestMoveAtRow(gameState, hand, row) }

        if(bestMove.move != null) { //Move exists
            player.exchangeStreak = 0

            val piecesToPlay = bestMove.move.pieces.toMutableList()
            val blanks = piecesToPlay.withIndex().filter { it.value.letter.isLowerCase() }
            val availableBlanks = hand.pieces.filter { it.letter == '_' }.toMutableList()

            for(blank in blanks) {
                val blankReplacement = availableBlanks.removeFirst()
                blankReplacement.letter = blank.value.letter.uppercaseChar()
                piecesToPlay[blank.index] = blankReplacement
            }

            return Move(bestMove.move.start, bestMove.move.direction, piecesToPlay)
        }

        if (gameState.bag.isEmpty()) return Pass() //No Move, no pieces to exchange

        if(player.exchangeStreak > 3) return Pass() //To avoid never ending the game, limit amount of exchanges in a row

        player.exchangeStreak++

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
        row: IndexedValue<List<Square>>
    ): MoveAndScore {
        var bestMove = MoveAndScore()

        //Handle blanks
        val blank = hand.pieces.find { '_' == it.letter } //finds the first occurrence
        if(blank != null) {
            return ('a'..'z').maxOf {//for blanks, try a hand with each possible letter(lowercase represents blanks)
                val blankReplacement = Piece(it, blank.value)
                bestMoveAtRow(
                    gameState,
                    Hand(hand.pieces - blank + blankReplacement),
                    row,
                )
            }
        }

        for((colNum, square) in row.value.withIndex().reversed()) {
            val coord = Coord(colNum, row.index)

            if(square.hasPiece()) continue

            //check both directions
            bestMove = maxOf(
                bestMove,
                bestMoveAtSpot(gameState.board, hand, Direction.DOWN, coord),
                bestMoveAtSpot(gameState.board, hand, Direction.ACROSS, coord, true)
            )
        }

        return bestMove
    }

    private fun bestMoveAtSpot(
        board: Board,
        hand: Hand,
        direction: Direction,
        coord: Coord,
        ignoreSingletons: Boolean = false
    ): MoveAndScore {
        val (minLength, maxLength) = getSpotInfo(board, coord, direction, hand.size())

        val startingWindow = board.findPrefix(coord, direction) + when (direction) {
            Direction.ACROSS -> board.matrix
                .getRow(coord.y)
                .slice(coord.x..<board.matrix.colCount)
                .map {it.piece}
            Direction.DOWN -> board.matrix
                .getCol(coord.x)
                .slice(coord.y..<board.matrix.rowCount)
                .map {it.piece}
            else -> throw Exception()
        }
        val wasAlreadyPlaced = startingWindow.map { it != null }

        if(minLength == null) return MoveAndScore()

        var bestMove = MoveAndScore()

        fun searchPermutations(turnWindow: List<Piece?>, remaining: List<Piece> = hand.pieces.toList()) {
            val prefix = turnWindow.takeWhile { it != null } as List<Piece>

            if(prefix.size+1 > maxLength) return //word too long

            //check if there's any valid words with this prefix
            if(!prefixes
                .contains(
                    prefix.joinToString("") { it.letter.toString().lowercase() }
                )
            ) return

            for(letter: Piece in remaining) {
                if(minLength <= prefix.size+1) { //try and score it if it's long enough
                    val move = Move(coord, direction, (prefix + letter).filterIndexed { i,_ -> !wasAlreadyPlaced[i] })
                    try{
                        val score = board.findMove(move).second //this should be the most expensive call
                        
                        if(score > bestMove.score) bestMove = MoveAndScore(move, score) //this accesses variable inside fun bestMoveAtSpot
                    } catch (_:Exception) {
                        
                    }
                }
                if(prefix.size >= turnWindow.size) continue //no more space to play
                searchPermutations(
                    turnWindow.toMutableList().apply { set(prefix.size, letter) },
                    remaining = remaining - letter
                )
            }
        }
        searchPermutations(startingWindow)
        return bestMove
    }


    private data class SpotInfo(val minMoveLength: Int?, val maxMoveLength: Int)
    private fun getSpotInfo(board: Board, loc: Coord, dir: Direction, numPieces: Int): SpotInfo {
        var minMoveLength: Int? = null
        var maxMoveLength = 0

        var curr = loc
        //check behind
        if(board.getOrNull(curr.plusParallel(-1, dir))?.hasPiece() == true) {
            minMoveLength = 1
        }

        var i = 0 //the distance from where you started
        while(board.getOrNull(curr) != null && maxMoveLength < numPieces) {
            if(!board[curr].hasPiece()) maxMoveLength++

            if(
                minMoveLength == null &&
                (curr == board.center() ||
                board.getOrNull(curr.plusParallel(1, dir))?.hasPiece() == true ||
                board.getOrNull(curr.plusPerpendicular(-1, dir))?.hasPiece() == true ||
                board.getOrNull(curr.plusPerpendicular(1, dir))?.hasPiece() == true)
            ) minMoveLength = i+1

            curr = curr.plusParallel(1, dir)
            i++
        }

        return SpotInfo(minMoveLength, maxMoveLength)
    }

    /**
     * List of pieces before [coord] opposite to [direction]
     *
     * Iterates backwards in the direction, finding the prefix from the pieces it finds before the coord
     */
    private fun Board.findPrefix(coord: Coord, direction: Direction): List<Piece> {
        var nextCoord = coord.plusParallel(-1, direction)
        val prefix = LinkedList<Piece>()
        while (nextCoord.x >= 0 && nextCoord.y >= 0 && this[nextCoord].hasPiece()) {
            prefix.addFirst(this[nextCoord].piece!!)

            nextCoord = nextCoord.plusParallel(-1, direction)
        }
        return prefix
    }

    /** Contains a move and score and allows for null moves (default with -1 score) */
    private data class MoveAndScore(val move: Move?, val score: Int): Comparable<MoveAndScore> {
        /** constructs null move and -1 score */
        constructor() : this(null, -1)

        override fun compareTo(other: MoveAndScore) = this.score.compareTo(other.score)
    }
}