package controllers

import exceptions.IllegalMoveException
import exceptions.InvalidWordException
import models.GameState
import models.board.Board
import models.board.Coord
import models.board.Multiplier
import models.board.Square
import models.tiles.Piece
import models.turn.Direction
import models.turn.Move
import views.ViewInput
import views.ViewOutput
import java.util.*

class GameController(val game: GameState, val viewInput: ViewInput, val viewOutput: ViewOutput) {
    fun nextMove() {
        val currentPlayer = game.currentPlayer()
        val turn = currentPlayer.playerController.getTurn(game, currentPlayer)
        game.makeTurn(turn)
    }

    fun scoreMove(board: Board, move: Move): Int {
        val placedWord = LinkedList<Piece>()
        var placedWordMultiplier = 1
        var placedWordScore = 0

        var totalScore = 0

        //counting each square
        val piecesToCount = move.pieces.toMutableList()
        val placeAt = LinkedList<Coord>()
        for ((currSqCoord, currSq) in traverseMoveWithCoords(move, board, true)) {
            if (currSq.hasPiece()) {
                placedWord.add(currSq.piece!!)
                totalScore += currSq.piece!!.value
            } else {
                val pieceToPlace = piecesToCount.removeFirst()
                placedWord.add(pieceToPlace)
                placeAt.addLast(currSqCoord)

                //check for a perpendicular word
                val perpWord = wordContainingCoord(
                    currSqCoord,
                    Direction.perpendicularTo(move.direction),
                    board,
                    pieceToPlace
                )

                //score perpendicular word
                var perpWordScore = perpWord.sumOf { it.value }
                when (currSq.multiplier) {
                    Multiplier.DOUBLE_LETTER -> {
                        perpWordScore += pieceToPlace.value //since we already added one in sumOf
                        placedWordScore += pieceToPlace.value * 2
                    }

                    Multiplier.TRIPLE_LETTER -> {
                        perpWordScore += pieceToPlace.value * 2
                        placedWordScore += pieceToPlace.value * 3
                    }

                    Multiplier.DOUBLE_WORD -> {
                        perpWordScore *= 2
                        placedWordMultiplier *= 2
                        placedWordScore += pieceToPlace.value
                    }

                    Multiplier.TRIPLE_WORD -> {
                        perpWordScore *= 3
                        placedWordMultiplier *= 3
                        placedWordScore += pieceToPlace.value
                    }

                    else -> placedWordScore += pieceToPlace.value
                }
                totalScore += perpWordScore
            }
        }

        //validate placed word
        if (!isValidScrabbleWord(placedWord.map { it.letter }.joinToString("")))
            throw InvalidWordException(placedWord.map { it.letter }.joinToString(""))

        placedWordScore *= placedWordMultiplier
        totalScore += placedWordScore

        //place the pieces
        val piecesToPlace = move.pieces.toMutableList()
        for (coord in placeAt) {
            val sq = board[coord]
            board[coord] = Square(sq.multiplier, piecesToPlace.removeFirst(), game.turnNum, game.currentPlayer())
        }

        //update scores
        game.currentPlayer().score += totalScore

        return totalScore
    }

    private fun isValidScrabbleWord(word: String): Boolean {
        return true //todo
    }

    //PRIVATE HELPER METHODS//
    private fun wordContainingCoord(
        coord: Coord,
        direction: Direction,
        board: Board,
        pieceToPlace: Piece? = null
    ): List<Piece> {
        val beginningOfPerpendicularWord = findBeginningOfWord(
            coord,
            direction,
            board
        )

        var perpWord = readWord(
            beginningOfPerpendicularWord,
            board,
            direction,
            addFakePieceAt = if (pieceToPlace == null) null else coord,
            addFakePiece = pieceToPlace
        )

        if (perpWord.size != 1 &&
            !isValidScrabbleWord(perpWord.map { it.letter }.joinToString(""))
        ) {
            throw InvalidWordException(perpWord.map { it.letter }.joinToString(""))
        }

        if (perpWord.size == 1) perpWord = LinkedList<Piece>()
        return perpWord
    }

    private fun findBeginningOfWord(coord: Coord, direction: Direction, board: Board): Coord {
        var currCord = coord
        var nextCoord = Coord(
            coord.x - if (direction == Direction.ACROSS) 1 else 0,
            coord.y - if (direction == Direction.DOWN) 1 else 0
        )
        while (board[nextCoord].hasPiece() && nextCoord.x > 0 && nextCoord.y > 0) {
            currCord = nextCoord
            nextCoord = Coord(
                coord.x - if (direction == Direction.ACROSS) 1 else 0,
                coord.y - if (direction == Direction.DOWN) 1 else 0
            )
        }
        return currCord
    }

    private fun readWord(
        startingCoord: Coord,
        board: Board,
        direction: Direction,
        addFakePieceAt: Coord? = null,
        addFakePiece: Piece? = null
    ): List<Piece> {
        if ((addFakePieceAt == null) xor (addFakePiece == null)) throw IllegalArgumentException()

        val wordBuilder = LinkedList<Piece>()
        var currentCoord = startingCoord
        while (currentCoord == addFakePieceAt ||
            (board[currentCoord].hasPiece() && currentCoord.x < board.size() && currentCoord.y < board.size())
        ) {
            if (addFakePieceAt != null && addFakePieceAt == currentCoord) {
                wordBuilder.add(addFakePiece!!)
            } else wordBuilder.add(board[currentCoord].piece!!)
            currentCoord = Coord(
                currentCoord.x + if (direction == Direction.ACROSS) 1 else 0,
                currentCoord.y + if (direction == Direction.DOWN) 1 else 0
            )
        }
        return wordBuilder
    }

    private fun traverseMoveWithCoords(
        move: Move,
        board: Board,
        includeBoardPieces: Boolean
    ): Iterator<Pair<Coord, Square>> {
        return object : Iterator<Pair<Coord, Square>> {
            var currentCoord = move.start
            val pieceList = LinkedList(move.pieces)

            override fun hasNext(): Boolean {
                if (!includeBoardPieces) return pieceList.isNotEmpty()
                if (currentCoord.x > board.size() || currentCoord.y > board.size()) throw IllegalMoveException()
                return pieceList.isNotEmpty() && board[currentCoord].piece != null
            }

            override fun next(): Pair<Coord, Square> {
                var currentSquare = board[currentCoord]
                var prevCoord = currentCoord

                if (includeBoardPieces) {
                    currentCoord = Coord(
                        currentCoord.x + if (move.direction == Direction.ACROSS) 1 else 0,
                        currentCoord.y + if (move.direction == Direction.DOWN) 1 else 0
                    )
                    return prevCoord to currentSquare
                }

                currentCoord = Coord(
                    currentCoord.x + if (move.direction == Direction.ACROSS) 1 else 0,
                    currentCoord.y + if (move.direction == Direction.DOWN) 1 else 0
                )

                while (currentSquare.piece != null) {
                    currentSquare = board[currentCoord]
                    prevCoord = currentCoord
                    currentCoord = Coord(
                        currentCoord.x + if (move.direction == Direction.ACROSS) 1 else 0,
                        currentCoord.y + if (move.direction == Direction.DOWN) 1 else 0
                    )
                }
                return prevCoord to currentSquare
            }
        }
    }


}