package models.board

class Board {
    //todo: Add multipliers
    val board = Array(15) { Array(15) { Square(Multiplier.NONE) } }

    fun get(coord: Coord) = board[coord.x][coord.y]

    fun set(coord: Coord, square: Square) {
        board[coord.x][coord.y] = square
    }
}