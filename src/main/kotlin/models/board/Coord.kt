package models.board

import models.turn.Direction

data class Coord(val x: Int, val y: Int) {
    fun add(coord: Coord) = Coord(x + coord.x, y + coord.y)
    fun add(x: Int, y: Int) = Coord(this.x + x, this.y + y)

    fun plusParallel(i: Int, dir: Direction): Coord = if(dir == Direction.ACROSS) this.add(i, 0) else add(0, i)
    fun plusPerpendicular(i: Int, dir: Direction): Coord = if(dir == Direction.ACROSS) this.add(0, i) else add(i, 0)

    override fun toString(): String = "($x, $y)"
}