package models.board

class Board(val board: Array<Array<Square>>) {
    //todo: Add multipliers
    constructor() : this(Array(15) { Array(15) { Square(Multiplier.NONE) } })

    fun classInv(): Boolean{
        return board.isNotEmpty() && board.size == board[0].size
    }

    fun get(coord: Coord) = board[coord.x][coord.y]

    fun set(coord: Coord, square: Square) {
        board[coord.x][coord.y] = square
    }

    /**
     * Width and height of the board
     */
    fun size() = board.size
}