package controllers.util

import models.tiles.Piece
import models.turn.Direction
import models.turn.Direction.ACROSS
import models.turn.Direction.DOWN
import java.util.*


fun String.isValidScrabbleWord(): Boolean {
    return Dictionary.contains(this)
}

fun Direction.perpendicular(): Direction {
    if (this == DOWN) return ACROSS
    return DOWN
}

fun StringBuilder.appendLn(text: String): java.lang.StringBuilder = append(text).append("\n")

fun parsePieceFile(path: String): List<Piece> {
    val pieces = mutableListOf<Piece>()
    val pieceFile = Scanner(java.io.File(path))
    while (pieceFile.hasNextLine()) {
        val line = pieceFile.nextLine().split(",")
        val pieceToAdd = Piece(line[0][0], line[2].toInt())
        repeat(line[1].toInt()) { pieces.add(pieceToAdd.copy()) }
    }
    return pieces
}