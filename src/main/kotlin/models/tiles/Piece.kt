package models.tiles

data class Piece(val letter: Char, val value: Int) {
    constructor(letter: Char) : this(letter, defaultPieceValues(letter))
}

fun defaultPieceValues(letter: Char): Int {
    return when (letter) {

    }
}