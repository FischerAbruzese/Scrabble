package models.turn

import models.board.Coord
import models.tiles.Piece


data class Move(
    val start: Coord,
    val direction: Direction,
    val pieces: List<Piece>
) : Turn {

    override fun toString(): String {
        return "Move(start=$start, direction=$direction, pieces=${pieces.joinToString("") { it.letter.toString() }})"
    }
}