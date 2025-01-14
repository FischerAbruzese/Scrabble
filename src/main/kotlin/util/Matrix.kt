package util

class Matrix<T>(private val rowCount: Int, private val colCount: Int, init: (Int, Int) -> T) {

    @Suppress("UNCHECKED_CAST")
    private val secretArray = Array<Any?>(rowCount * colCount) { i ->
        val (row, col) = getRowCol(i)
        init(row, col)
    } as Array<T>
    private fun getRowCol(index: Int) = index / colCount to index % colCount
    private fun getIndex(row: Int, col: Int) = row*colCount + col

    operator fun get(row: Int, col: Int) = secretArray[getIndex(row, col)]
    operator fun set(row: Int, col: Int, value: T) { secretArray[getIndex(row, col)] = value }
}