package controllers.players

import exceptions.IllegalMoveException
import models.GameState
import models.Player
import models.board.Board
import models.board.Coord
import models.tiles.Hand
import models.tiles.Piece
import models.turn.*
import util.Dictionary

class AI : PlayerController {
    companion object {
        val prefixes = HashSet<String>().apply {
            for(word in Dictionary.words) {
                for (i in 1..word.length) {
                    add(word.substring(0, i))
                }
            }
        }
    }

    //AI Parameters
    /**
     * Minimum score of a move to be considered. Must be greater than 0
     */
    private val moveScoreCutoff = 3

    /**
     * A potential move and its score
     */
    class PotentialMove(val move: Move?, val score: Int = -1) : Comparable<PotentialMove> {
        override fun compareTo(other: PotentialMove): Int = score.compareTo(other.score)
    }

    override fun getTurn(gameState: GameState, player: Player): Turn {
        val potentialMove = maxOf(
            bestMove(gameState, player.hand, Direction.ACROSS),
            bestMove(gameState, player.hand, Direction.DOWN),
        )

        potentialMove.move?.let { return it }

        if (gameState.bag.isEmpty()) return Pass()

        return Exchange(player.hand.pieces)//TODO: limited pieces in bag
    }

    /**
     * Finds the best move in the given direction for the given player
     *
     * @return the best move and its score
     */
    private fun bestMove(gameState: GameState, hand: Hand, direction: Direction): PotentialMove {
        val boardSize = gameState.board.size()
        var bestMove = PotentialMove(null, -1)

        //Replace underscores
        val underscoreLocation = hand.indexOf('_')
        if (underscoreLocation >= 0) {
            return ('a'..'z').maxOf { c ->
                bestMove(
                    gameState,
                    hand.deepCopy().apply{ pieces[underscoreLocation].letter = c},
                    direction
                )
            }
        }

        assert(hand.indexOf('_') == -1)

        for ((rowIndex, row) in gameState.board.rows().withIndex()) {
            var maxPieces = 0 //Number of empty squares between current spot and right edge
            for ((backwardsColIndex, square) in row.reversed().withIndex()) {
                if (square.hasPiece()) {
                    continue
                }
                maxPieces++


                val coord = Coord(rowIndex, boardSize - backwardsColIndex - 1)
                bestMove = maxOf (
                    bestMove,
                    bestMoveAtSpot(
                        gameState.board,
                        hand,
                        direction,
                        coord,
                        maxPieces
                    )
                )
            }
        }
        return bestMove
    }

    /**
     * Minimum length for a word to be valid
     *
     * @return the minimum length for a word to be valid or -1 if no length less than or equal to [numPieces] is valid
     */
    private fun findMinLength(board: Board, loc: Coord, numPieces: Int = 7): Int {
        var curr = loc
        //check behind
        if(board[curr.plusX(-1)].hasPiece()) return 1

        for(i in 1..numPieces) {
            if(
                board[curr.plusX(1)].hasPiece() ||
                board[curr.plusY(-1)].hasPiece() ||
                board[curr.plusY(1)].hasPiece()
            ) return i
        }
        return -1
    }

    private fun bestMoveAtSpotRecursive(board: Board, hand: Hand, direction: Direction, coord: Coord, maxLength : Int): PotentialMove {
        val minLength = findMinLength(board, coord, hand.size())

        if(minLength == -1) return PotentialMove(null)

        var bestMove = PotentialMove(null, -1)
        fun searchPermutations(prefix: List<Piece> = listOf(), remaining: List<Piece> = hand.pieces.toList()) {
            if(!prefixes.contains(prefix.joinToString("") { it.letter.toString() })) return
            for(letter in remaining) {
                if(minLength < prefix.size) {
                    val move = Move(coord, direction, prefix + letter)
                    val score = try{ board.findMove(move).second } catch (_:Exception) { null }
                    if(score != null && score > bestMove.score)
                        bestMove = PotentialMove(move, score)
                }

                searchPermutations(prefix + letter, remaining - letter)
            }
        }
        searchPermutations()

        return bestMove //MAYBE NOT DONE HERE
    }


    private fun bestMoveAtSpot(board: Board, hand: Hand, direction: Direction, coord: Coord, maxLength : Int): PotentialMove{
        val minLength = findMinLength(board, coord, hand.size())

        if(minLength == -1) return PotentialMove(null)

        var bestMove = PotentialMove(null, -1)
        //try every possible permutation of hand at that spot pruning at max length, prefix, skip before min length
        var lettersLeft = Array(hand.size()){ mutableListOf<Piece>() }
        lettersLeft[0] = hand.pieces.toMutableList()

        val word = mutableListOf<Piece>()

        while(word.isNotEmpty() && lettersLeft[0].isNotEmpty()){
            try {
                word += lettersLeft[word.size].removeFirst()
                lettersLeft[word.size + 1] = word
            } catch(_: NoSuchElementException){
                word.dropLast(2)
                continue
            }
            if(!prefixes.contains(word.map{it.letter}.joinToString(""))) {
                word.dropLast(1)
            }
            val move = Move(coord, direction, word)
            try {
                val attemptedMove: Pair<List<Coord>, Int> = board.findMove(move)
                bestMove = maxOf(bestMove, PotentialMove(move, attemptedMove.second))
            } catch(_: IllegalMoveException){}
        }


        return maxOf(hand.pieces.mapIndexed{ index, piece -> bestMoveAtSpot(gameState, hand.copy().apply{pieces.removeAt(index)}, direction, coord, minLength, maxLength, word.plus(piece.letter)) })
    }

    //TODO: add removal to trie
}