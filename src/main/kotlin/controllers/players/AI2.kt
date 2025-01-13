package main.kotlin.controllers.players

import controllers.players.PlayerController
import kotlinx.coroutines.Delay
import models.GameState
import models.Player
import models.board.Board
import models.board.Coord
import models.board.Square
import models.tiles.Hand
import models.tiles.Piece
import models.turn.Direction
import models.turn.Move
import models.turn.Pass
import models.turn.Turn
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
        var bestMove = MoveAndScore()

        //Search every row for the best scoring move in that row
        for(rowWithIndex in gameState.board.rows().withIndex()) {
            bestMove = checkRow(gameState, hand, rowWithIndex, bestMove)
        }

        return bestMove.move ?: Pass()
    }

    /** @return the max of bestMoveAtSpot for each spot in both directions or previousBestMove, handles blank tiles */
    private fun checkRow(
        gameState: GameState,
        hand: Hand,
        row: IndexedValue<Array<Square>>,
        previousBestMove: MoveAndScore //can we prune early by knowing this?
    ): MoveAndScore {

        var rowBest = MoveAndScore()

        for((colNum, square) in row.value.withIndex()) {
            val coord = Coord(colNum, row.index)
            if(square.hasPiece()) continue

            //for blanks, try a hand with each possible letter
            val blank = hand.pieces.find { '_' == it.letter } //finds the first occurrence
            if(blank != null) {
                return ('a'..'z').maxOf {
                    checkRow(
                        gameState,
                        Hand(hand.pieces - blank + Piece(it, 1)),
                        row,
                        previousBestMove
                    )
                }
            }

            //check both directions
            rowBest = maxOf(
                rowBest,
                bestMoveAtSpot(gameState.board, hand, Direction.DOWN, coord, row.value.size - colNum),
                bestMoveAtSpot(gameState.board, hand, Direction.ACROSS, coord, row.value.size - colNum, true)
            )
        }

        return maxOf(rowBest, previousBestMove)
    }

    private fun bestMoveAtSpot(
        board: Board,
        hand: Hand,
        direction: Direction,
        coord: Coord,
        maxLength : Int,
        ignoreSingletons: Boolean = false
    ): MoveAndScore {
        var minLength = findMinLength(board, coord, direction, hand.size())
            ?: return MoveAndScore()

        if(ignoreSingletons && minLength == 1) minLength = 2 //singletons might have been checked in other direction

        var bestMove = MoveAndScore()
        fun searchPermutations(prefix: List<Piece> = listOf(), remaining: List<Piece> = hand.pieces.toList()) {
            if(prefix.size >= maxLength) return
            //check if there's any valid words with this prefix
            if(!prefixes.contains(prefix.joinToString("") { it.letter.toString() })) return

            for(letter in remaining) {
                if(minLength < prefix.size) { //try and score it if it's long enough
                    val move = Move(coord, direction, prefix + letter)
                    val score = try{ board.findMove(move).second } catch (_:Exception) { null } //this should be the most expensive call
                    if(score != null && score > bestMove.score)
                        bestMove = MoveAndScore(move, score) //this accesses variable inside fun bestMoveAtSpot
                }

                searchPermutations(prefix + letter, remaining - letter)
            }
        }
        searchPermutations(board.findPrefix(coord, direction))

        return bestMove //MAYBE NOT DONE HERE
    }

    /** @return minimum word size to be able to be legally placed, null if it's greater than handSize */
    private fun findMinLength(board: Board, coord: Coord, direction: Direction, handSize: Int): Int? {
        var incrementCoord = Coord(
            if (direction == Direction.ACROSS) -1 else 0,
            if (direction == Direction.DOWN) -1 else 0
        )
        //before
        if(board.getOrNull(coord.add(incrementCoord))?.hasPiece() == true) return 1

        for(i in 0..<handSize) {
            val currentLoc = coord.add(
                if (direction == Direction.ACROSS) i else 0,
                if (direction == Direction.DOWN) i else 0
            )

            if(currentLoc == board.center()) return i

            val inFront = board.getOrNull(currentLoc.add(
                if (direction == Direction.ACROSS) 1 else 0,
                if (direction == Direction.DOWN) 1 else 0
            ))
            if(inFront?.hasPiece() == true) return i

            val perpendicular = board.getOrNull(currentLoc.add(
                if (direction == Direction.ACROSS) 0 else 1,
                if (direction == Direction.DOWN) 0 else 1
            ))
            if(perpendicular?.hasPiece() == true) return i

            val oppositePerpendicular = board.getOrNull(currentLoc.add(
                if (direction == Direction.ACROSS) 0 else -1,
                if (direction == Direction.DOWN) 0 else -1
            ))
            if(oppositePerpendicular?.hasPiece() == true) return i
        }
        return null
    }

    /** iterates backwards in the direction, finding the prefix from the pieces it finds before the coord */
    private fun Board.findPrefix(coord: Coord, direction: Direction): List<Piece> {
        var nextCoord = Coord(
            coord.x - if (direction == Direction.ACROSS) 1 else 0,
            coord.y - if (direction == Direction.DOWN) 1 else 0
        )

        val prefix = LinkedList<Piece>()
        while (nextCoord.x > 0 && nextCoord.y > 0 && this[nextCoord].hasPiece()) {
            prefix.addFirst(this[nextCoord].piece!!)

            nextCoord = Coord(
                coord.x - if (direction == Direction.ACROSS) 1 else 0,
                coord.y - if (direction == Direction.DOWN) 1 else 0
            )
        }
        return prefix
    }

    /** Contains a move and score and allows for null moves (default with -1 score) */
    private data class MoveAndScore(val move: Move?, val score: Int): Comparable<MoveAndScore> {
        /** constructs null move and -1 score */ constructor() : this(null, -1)

        override fun compareTo(other: MoveAndScore) = this.score.compareTo(other.score)
    }
}