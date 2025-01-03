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
        val currentWord = LinkedList<Piece>()
        var wordMultiplier = 1
        var wordScore = 0

        var totalScore = 0

        val piecesToCount = move.pieces.toMutableList()
        val placeAt = LinkedList<Coord>()
        for ((coord, square) in traverseMoveWithCoords(move, board, true)) {

            if (square.hasPiece()) {
                currentWord.add(square.piece!!)
                totalScore += square.piece!!.value
            } else {
                val pieceToPlace = piecesToCount.removeFirst()
                currentWord.add(pieceToPlace)
                placeAt.addLast(coord)

                //check for a perpendicular word
                val beginningOfPerpendicularWord = findBeginningOfWord(
                    coord,
                    if (move.direction == Direction.ACROSS) Direction.DOWN else Direction.ACROSS,
                    board
                )

                var perpWord = readWord(
                    beginningOfPerpendicularWord,
                    board,
                    if (move.direction == Direction.ACROSS) Direction.DOWN else Direction.ACROSS,
                    addFakePieceAt = coord,
                    addFakePiece = pieceToPlace
                )

                if (perpWord.size != 1 &&
                    !isValidScrabbleWord(perpWord.map { it.letter }.joinToString(""))
                ) {
                    throw InvalidWordException(perpWord.map { it.letter }.joinToString(""))
                }

                if (perpWord.size == 1) perpWord = LinkedList<Piece>()

                //score perpendicular word
                var perpWordScore = perpWord.sumOf { it.value }
                when (square.multiplier) {
                    Multiplier.DOUBLE_LETTER -> {
                        perpWordScore += pieceToPlace.value //since we already added one in sumOf
                        wordScore += pieceToPlace.value * 2
                    }

                    Multiplier.TRIPLE_LETTER -> {
                        perpWordScore += pieceToPlace.value * 2
                        wordScore += pieceToPlace.value * 3
                    }

                    Multiplier.DOUBLE_WORD -> {
                        perpWordScore *= 2
                        wordMultiplier *= 2
                        wordScore += pieceToPlace.value
                    }

                    Multiplier.TRIPLE_WORD -> {
                        perpWordScore *= 3
                        wordMultiplier *= 3
                        wordScore += pieceToPlace.value
                    }

                    else -> wordScore += pieceToPlace.value
                }
                totalScore += perpWordScore
            }
        }

        if (!isValidScrabbleWord(currentWord.map { it.letter }.joinToString("")))
            throw InvalidWordException(currentWord.map { it.letter }.joinToString(""))

        wordScore *= wordMultiplier
        totalScore += wordScore

        val piecesToPlace = move.pieces.toMutableList()
        for (coord in placeAt) {
            val sq = board[coord]
            board[coord] = Square(sq.multiplier, piecesToPlace.removeFirst(), game.turnNum, game.currentPlayer())
        }
        game.currentPlayer().score += totalScore
        return totalScore
    }

    fun isValidScrabbleWord(word: String): Boolean {
        return true //todo
    }

    //PRIVATE HELPER METHODS//

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