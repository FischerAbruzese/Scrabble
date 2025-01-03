package controllers

import exceptions.InvalidWordException
import models.GameState
import models.board.Board
import models.board.Coord
import models.board.Square
import models.turn.Direction
import models.turn.Move
import views.ViewInput
import views.ViewOutput
import java.util.*

class GameController(val game: GameState, val viewInput: ViewInput, val viewOutput: ViewOutput) {
    fun nextMove() {
        val currentPlayer = game.nextPlayer()
        val turn = currentPlayer.playerController.getTurn(game, currentPlayer)
        game.makeTurn(turn)
    }

    private fun findBeginningOfWord(coord: Coord, direction: Direction, board: Board): Coord {
        var currCord = coord
        var nextCoord = Coord(
            coord.x + if (direction == Direction.ACROSS) 1 else 0,
            coord.y + if (direction == Direction.DOWN) 1 else 0
        )
        while (board.get(nextCoord).hasPiece() && nextCoord.x > 0 && nextCoord.y > 0) {
            currCord = nextCoord
            nextCoord = Coord(
                coord.x + if (direction == Direction.ACROSS) 1 else 0,
                coord.y + if (direction == Direction.DOWN) 1 else 0
            )
        }
        return currCord
    }


    fun scoreMove(board: Board, move: Move): Int {
        val currentWord = StringBuilder()
        var wordMultiplier = 1
        var score = 0

        val piecesToPlace = move.pieces.toMutableList()
        val placeAt = hashSetOf<Coord>()
        for ((coord, square) in traverseMoveWithCoords(move, board, true)) {
            if (square.piece == null) {
                val pieceToPlace = piecesToPlace.removeFirst()
                if (move.direction == Direction.ACROSS) {
                    var currentCoord = Coord(coord.x, coord.y - 1)
                    while (board.get(currentCoord).piece != null && currentCoord.y > 0) {
                        currentCoord = Coord(currentCoord.x, currentCoord.y - 1)
                    }
                    if (currentCoord.y > 0) currentCoord = Coord(currentCoord.x, currentCoord.y + 1)

                    if (currentCoord != coord) {
                        val perpendicularWordBuilder = StringBuilder()
                        while (board.get(currentCoord).piece != null && currentCoord.y <= 15) {
                            if (currentCoord == coord) perpendicularWordBuilder.append(pieceToPlace.letter)
                            else perpendicularWordBuilder.append(board.get(currentCoord).piece!!.letter)
                            currentCoord = Coord(currentCoord.x, currentCoord.y + 1)
                        }
                        if (!isValidScrabbleWord(perpendicularWordBuilder.toString())) throw InvalidWordException()
                    }
                }
            }

            //categorize to already placed or about to be placed
            //
        }

//
//            score += when (currentSquare.multiplier) {
//                Multiplier.DOUBLE_LETTER -> currentPiece.value * 2
//                Multiplier.TRIPLE_LETTER -> currentPiece.value * 3
//                Multiplier.DOUBLE_WORD -> currentPiece.value.also { wordMultiplier *= 2 }
//                Multiplier.TRIPLE_WORD -> currentPiece.value.also { wordMultiplier *= 3 }
//                Multiplier.NONE -> currentPiece.value
//            }
//        }

    }

    fun isValidScrabbleWord(word: String): Boolean {
        return true //todo
    }

    fun traverseMoveWithCoords(move: Move, board: Board, includeBoardPieces: Boolean): Iterator<Pair<Coord, Square>> {
        return object : Iterator<Pair<Coord, Square>> {
            var currentCoord = move.start
            val pieceList = LinkedList(move.pieces)

            override fun hasNext(): Boolean {
                if (!includeBoardPieces) return pieceList.isNotEmpty()
                return pieceList.isNotEmpty() && board.get(currentCoord).piece != null
            }

            override fun next(): Pair<Coord, Square> {
                var currentSquare = board.get(currentCoord)
                var prevCoord = currentCoord

                if (!includeBoardPieces) {
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
                    currentSquare = board.get(currentCoord)
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