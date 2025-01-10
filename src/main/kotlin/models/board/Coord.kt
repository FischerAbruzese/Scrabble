package models.board

data class Coord(val x: Int, val y: Int){
    /**
     * Returns a new coord shifted in the x direction by [shift]
     */
    fun plusX(shift: Int) = Coord(x + shift, y)
    /**
     * Returns a new coord shifted in the y direction by [shift]
     */
    fun plusY(shift: Int) = Coord(x, y + shift)
}