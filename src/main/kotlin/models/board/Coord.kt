package models.board

data class Coord(val x: Int, val y: Int) {
    fun add(coord: Coord) = Coord(x + coord.x, y + coord.y)
    fun add(x: Int, y: Int) = Coord(this.x + x, this.y + y)

    override fun toString(): String = "($x, $y)"
}