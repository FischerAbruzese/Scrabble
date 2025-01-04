package models.board

import controllers.util.isValidScrabbleWord
import controllers.util.perpendicular
import exceptions.IllegalMoveException
import exceptions.InvalidWordException
import models.tiles.Piece
import models.turn.Direction
import models.turn.Move
import java.util.*

class Board(val board: Array<Array<Square>>) {
    companion object {
        private val EMPTY_SQUARE = Square(Multiplier.NONE)
    }

    //todo: Add multipliers
    constructor() : this(Array(15) { Array(15) { EMPTY_SQUARE } })

    fun classInv(): Boolean {
        return board.isNotEmpty() && board.size == board[0].size
    }

    operator fun get(coord: Coord) = board[coord.x][coord.y]

    operator fun set(coord: Coord, square: Square) {
        board[coord.x][coord.y] = square
    }

    fun isValidCoordinate(coord: Coord): Boolean {
        return coord.x < size() && coord.y < size() && coord.x >= 0 && coord.y >= 0
    }

    /**
     * Width and height of the board
     */
    fun size() = board.size

    fun readWord(
        startingCoord: Coord,
        direction: Direction,
    ): List<Piece> {
        val wordBuilder = LinkedList<Piece>()
        var currentCoord = startingCoord
        while (currentCoord.x < size() && currentCoord.y < size() && this[currentCoord].hasPiece()) {
            wordBuilder.add(this[currentCoord].piece!!)
            currentCoord = Coord(
                currentCoord.x + if (direction == Direction.ACROSS) 1 else 0,
                currentCoord.y + if (direction == Direction.DOWN) 1 else 0
            )
        }
        return wordBuilder
    }

    fun findWordAt(coord: Coord, direction: Direction): List<Piece> {
        val beginningOfPerpendicularWord = findBeginningOfWord(
            coord,
            direction
        )

        var perpWord = readWord(
            beginningOfPerpendicularWord,
            direction,
        )

        if (perpWord.size != 1 &&
            !(perpWord.map { it.letter }.joinToString("")).isValidScrabbleWord()
        ) {
            throw InvalidWordException(perpWord.map { it.letter }.joinToString(""))
        }

        if (perpWord.size == 1) perpWord = LinkedList<Piece>()
        return perpWord
    }

    private fun findBeginningOfWord(coord: Coord, direction: Direction): Coord {
        var currCord = coord
        var nextCoord = Coord(
            coord.x - if (direction == Direction.ACROSS) 1 else 0,
            coord.y - if (direction == Direction.DOWN) 1 else 0
        )
        while (nextCoord.x > 0 && nextCoord.y > 0 && this[nextCoord].hasPiece()) {
            currCord = nextCoord
            nextCoord = Coord(
                coord.x - if (direction == Direction.ACROSS) 1 else 0,
                coord.y - if (direction == Direction.DOWN) 1 else 0
            )
        }
        return currCord
    }

    fun removePieceAt(coord: Coord) {
        set(coord, Square(get(coord).multiplier, null, null, null))
    }

    /**
     * Attempts to place a word on the board
     *
     * @return a pair of the list of coordinates and the score of the placed word
     * @throws IllegalMoveException if the move is illegal
     */
    fun findMove(move: Move): Pair<List<Coord>, Int> {
        val boardClone = Board(board.map { it.clone() }.toTypedArray())
        boardClone.run {
            val placedSquares = LinkedList<Coord>()

            var currentLocation = move.start

            if (get(currentLocation).hasPiece())
                throw IllegalMoveException("Can not start move on a square with a piece")

            var placedWordScore = 0
            var placedWordMultiplier = 1

            //place all the tiles
            for (piece in move.pieces) {
                while (isValidCoordinate(currentLocation) && get(currentLocation).hasPiece()) {
                    val sq = get(currentLocation)
                    placedWordScore += when (sq.multiplier) {
                        Multiplier.NONE -> sq.piece!!.value
                        Multiplier.DOUBLE_LETTER -> sq.piece!!.value * 2
                        Multiplier.TRIPLE_LETTER -> sq.piece!!.value * 3
                        Multiplier.DOUBLE_WORD -> {
                            placedWordMultiplier *= 2
                            sq.piece!!.value
                        }

                        Multiplier.TRIPLE_WORD -> {
                            placedWordMultiplier *= 3
                            sq.piece!!.value
                        }
                    }
                    currentLocation = when (move.direction) {
                        Direction.ACROSS -> Coord(currentLocation.x + 1, currentLocation.y)
                        Direction.DOWN -> Coord(currentLocation.x, currentLocation.y + 1)
                        Direction.NONE -> throw IllegalStateException("Something has gone terribly wrong")
                    }
                }
                if (!isValidCoordinate(currentLocation)) {
                    throw IllegalMoveException("Move is out of bounds")
                }
                placedSquares.add(currentLocation)
                currentLocation = when (move.direction) {
                    Direction.ACROSS -> Coord(currentLocation.x + 1, currentLocation.y)
                    Direction.DOWN -> Coord(currentLocation.x, currentLocation.y + 1)
                    Direction.NONE -> currentLocation
                }
            }

            var totalScore = 0

            //validate move and score
            val placedWord = findWordAt(move.start, move.direction)

            if (!placedWord.joinToString { it.letter.toString() }.isValidScrabbleWord()) {
                throw IllegalMoveException("Invalid word: $placedWord")
            }

            //validate perpendicular moves
            for (coord in placedSquares) {
                val word =
                    findWordAt(coord, move.direction.perpendicular())

                //score word
                var wordScore = 0
                var wordMultiplier = 1

                wordScore += word.sumOf { it.value }
                when (get(coord).multiplier) {
                    Multiplier.DOUBLE_LETTER -> get(coord).piece!!.value
                    Multiplier.TRIPLE_LETTER -> get(coord).piece!!.value * 2
                    Multiplier.DOUBLE_WORD -> wordMultiplier *= 2
                    Multiplier.TRIPLE_WORD -> wordMultiplier *= 3
                    Multiplier.NONE -> {}
                }
                totalScore += (wordScore * wordMultiplier)

                if (!word.joinToString { it.letter.toString() }.isValidScrabbleWord()) {
                    throw IllegalMoveException("Invalid word: $word")
                }
            }
            return placedSquares to totalScore
        }
    }
}