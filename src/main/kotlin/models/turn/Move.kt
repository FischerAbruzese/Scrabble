package models.turn

import models.board.Coord
import models.tiles.Piece


class Move(
    val start: Coord,
    val direction: Direction,
    val pieces: List<Piece>
) : Turn