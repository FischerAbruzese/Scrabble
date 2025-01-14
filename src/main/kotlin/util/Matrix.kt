package util

class Matrix<T>(rowCount: Int, colCount: Int, init: (Int, Int) -> T) {

    @Suppress("UNCHECKED_CAST")
    private val secretMatrix = Array(rowCount) { r ->
        Array<Any?>(colCount) { c -> init(r,c) }
    } as Array<Array<T>>


}