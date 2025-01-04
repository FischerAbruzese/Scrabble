package models.board

import controllers.util.isValidScrabbleWord
import exceptions.InvalidWordException
import models.tiles.Piece
import models.turn.Direction
import java.util.*

class Board(val board: Array<Array<Square>>) {
    //todo: Add multipliers
    constructor() : this(Array(15) { Array(15) { Square(Multiplier.NONE) } })

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
        while (this[currentCoord].hasPiece() && currentCoord.x < size() && currentCoord.y < size()) {
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
        while (this[nextCoord].hasPiece() && nextCoord.x > 0 && nextCoord.y > 0) {
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


}