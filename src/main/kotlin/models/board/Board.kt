package models.board

import exceptions.BoardPieceNotUsedException
import exceptions.IllegalMoveException
import exceptions.InvalidWordException
import models.tiles.Piece
import models.turn.Direction
import models.turn.Move
import util.isValidScrabbleWord
import util.perpendicular
import java.util.*

class Board(val board: Array<Array<Square>>): Iterable<Square> {
    companion object {
        //private val EMPTY_SQUARE = Square(Multiplier.NONE)

        private fun getMultiplier(row: Int, col: Int): Multiplier {
            // Center square
            if (row == 7 && col == 7) return Multiplier.DOUBLE_WORD

            // Triple Word Scores
            if (isTripleWordSquare(row, col)) return Multiplier.TRIPLE_WORD

            // Double Word Scores
            if (isDoubleWordSquare(row, col)) return Multiplier.DOUBLE_WORD

            // Triple Letter Scores
            if (isTripleLetterSquare(row, col)) return Multiplier.TRIPLE_LETTER

            // Double Letter Scores
            if (isDoubleLetterSquare(row, col)) return Multiplier.DOUBLE_LETTER

            return Multiplier.NONE
        }

        private fun isTripleWordSquare(row: Int, col: Int): Boolean {
            // Corner squares and middle edges
            return (row == 0 || row == 7 || row == 14) && (col == 0 || col == 7 || col == 14) && // Corners and middle edges
                    !(row == 7 && col == 7) // Exclude center square
        }

        private fun isDoubleWordSquare(row: Int, col: Int): Boolean {
            // Diagonal positions from corners until before premium squares
            return (row == col || row == 14 - col) && // Diagonal positions
                    row != 0 && row != 14 && // Exclude corners
                    (row < 5 || row > 9) // Only include positions before premium squares
        }

        private fun isTripleLetterSquare(row: Int, col: Int): Boolean {
            val positions = listOf(
                Coord(1, 5), Coord(1, 9),
                Coord(5, 1), Coord(5, 5), Coord(5, 9), Coord(5, 13),
                Coord(9, 1), Coord(9, 5), Coord(9, 9), Coord(9, 13),
                Coord(13, 5), Coord(13, 9)
            )
            return positions.contains(Coord(row, col))
        }

        private fun isDoubleLetterSquare(row: Int, col: Int): Boolean {
            val positions = listOf(
                Coord(0, 3), Coord(0, 11),
                Coord(2, 6), Coord(2, 8),
                Coord(3, 0), Coord(3, 7), Coord(3, 14),
                Coord(6, 2), Coord(6, 6), Coord(6, 8), Coord(6, 12),
                Coord(7, 3), Coord(7, 11),
                Coord(8, 2), Coord(8, 6), Coord(8, 8), Coord(8, 12),
                Coord(11, 0), Coord(11, 7), Coord(11, 14),
                Coord(12, 6), Coord(12, 8),
                Coord(14, 3), Coord(14, 11)
            )
            return positions.contains(Coord(row, col))
        }
    }

    constructor() : this(Array(15) { row ->
        Array(15) { col ->
            Square(getMultiplier(row, col))
        }
    })

    @Suppress("unused")
    fun classInv(): Boolean {
        return board.isNotEmpty() && board.size == board[0].size
                && board.size % 2 == 1 //Board must be odd
    }

    fun center(): Coord {
        return Coord(size() / 2, size() / 2)
    }

    /**
     * Gets the square at the given coordinate
     */
    operator fun get(coord: Coord) = board[coord.y][coord.x]

    /**
     * Gets the square at the given coordinate. Returns null if the coordinate is out of bounds
     */
    fun getOrNull(coord: Coord): Square? {
        try{
            return get(coord)
        }
        catch (e: Exception){
            return null
        }
    }

    operator fun set(coord: Coord, square: Square) {
        board[coord.y][coord.x] = square
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

    @Throws(InvalidWordException::class)
    fun findWordAt(coord: Coord, direction: Direction): List<Piece> {
        val beginningOfWord = findBeginningOfWord(
            coord,
            direction
        )

        var word = readWord(
            beginningOfWord,
            direction,
        )

        if (word.size != 1 &&
            !(word.map { it.letter }.joinToString("")).isValidScrabbleWord()
        ) {
            throw InvalidWordException("${word.map { it.letter }.joinToString("")} is not in the dictionary")
        }

        if (word.size == 1) word = LinkedList<Piece>()
        return word
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
    @Throws(IllegalMoveException::class)
    @Suppress("shadowed")
    fun findMove(move: Move): Pair<List<Coord>, Int> {
        val boardClone = Board(board.map { it.clone() }.toTypedArray())
        boardClone.run {
            var move = move
            val placedSquares = LinkedList<Coord>()

            var currentLocation = move.start

            if (get(currentLocation).hasPiece())
                throw IllegalMoveException("Can not start move on a square with a piece")

            var placedWordScore = 0
            var placedWordMultiplier = 1

            if (move.pieces.size == 1) {
                //if there's a tile to the left or right, our word is across
                if (getOrNull(Coord(currentLocation.x - 1, currentLocation.y))?.hasPiece() == true || getOrNull(Coord(currentLocation.x + 1, currentLocation.y))?.hasPiece() == true) {
                    move = Move(move.start, Direction.ACROSS, move.pieces)
                }
                //if there's a tile above or below, our word is down
                if (getOrNull(Coord(currentLocation.x, currentLocation.y - 1))?.hasPiece() == true || getOrNull(Coord(currentLocation.x, currentLocation.y + 1))?.hasPiece() == true) {
                    move = Move(move.start, Direction.DOWN, move.pieces)
                }
            }

            val prefixEnd = Coord(
                if (move.direction == Direction.ACROSS) currentLocation.x - 1 else currentLocation.x,
                if (move.direction == Direction.DOWN) currentLocation.y - 1 else currentLocation.y
            )
            if (get(prefixEnd).hasPiece()) {
                //go to beginning of prefix
                currentLocation = findBeginningOfWord(
                    prefixEnd,
                    move.direction
                )
            }


            //place all the tiles
            var usesBoardPiece = false
            for (piece in move.pieces) {
                while (isValidCoordinate(currentLocation) && get(currentLocation).hasPiece()) {
                    val sq = get(currentLocation)
                    placedWordScore += sq.piece!!.value
                    usesBoardPiece = true

                    currentLocation = when (move.direction) {
                        Direction.ACROSS -> Coord(currentLocation.x + 1, currentLocation.y)
                        Direction.DOWN -> Coord(currentLocation.x, currentLocation.y + 1)
                        Direction.NONE -> throw IllegalStateException("Something has gone terribly wrong")
                    }
                }
                if (!isValidCoordinate(currentLocation)) {
                    throw IllegalMoveException("Move is out of bounds")
                }

                set(
                    currentLocation,
                    Square(get(currentLocation).multiplier, piece, null, null)
                )
                placedSquares.add(currentLocation)
                //update score
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
                    Direction.NONE -> currentLocation
                }
            }

            var totalScore = placedWordScore * placedWordMultiplier

            //validate move and score
            val placedWord = findWordAt(move.start, move.direction)

            if (!placedWord.joinToString("") { it.letter.toString() }.isValidScrabbleWord()) {
                throw IllegalMoveException("Invalid word: ${placedWord.joinToString { it.letter.toString() }}")
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

                if(word.size > 1) usesBoardPiece = true

                if (word.size > 1 && !word.joinToString("") { it.letter.toString() }.isValidScrabbleWord()) {
                    throw IllegalMoveException("Invalid word: ${word.joinToString("") { it.letter.toString() }}")
                }
            }

            if (!usesBoardPiece && !placedSquares.contains(center()))
                throw BoardPieceNotUsedException("Move must use a board piece")

            return placedSquares to totalScore
        }
    }

    override fun iterator(): Iterator<Square> {
        return board.flatten().iterator()
    }
}