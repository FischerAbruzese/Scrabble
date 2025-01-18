package util

/**
 * A representation of a 0-indexed rectangular 2D array
 */
class Matrix<T> private constructor(
    val rowCount: Int,
    val colCount: Int,
    private val secretArray: Array<T>
): Iterable<T>, Cloneable {

    @Suppress("UNCHECKED_CAST")
    constructor(rowCount: Int, colCount: Int, init: (Int, Int) -> T): this(rowCount, colCount,
        Array<Any?>(rowCount * colCount) { i ->
            val (row, col) = i / colCount to i % colCount
            init(row, col)
        } as Array<T>)

    private fun getRowCol(index: Int) = index / colCount to index % colCount
    private fun getIndex(row: Int, col: Int) = row*colCount + col

    /**
     * Checks if the given [row] and [col] are in bounds of this array.
     *
     * @throws IndexOutOfBoundsException If the [row] or [col] is out of bounds of this array.
     */
    private fun checkBounds(row: Int, col: Int) {
        if(row < 0) throw IndexOutOfBoundsException("row index $row is out of bounds for length $rowCount")
        if(col < 0) throw IndexOutOfBoundsException("col index $col is out of bounds for length $colCount")
        if(col >= colCount) throw IndexOutOfBoundsException("col index $col is out of bounds for length $colCount")
        if(row >= rowCount) throw IndexOutOfBoundsException("row index $row is out of bounds for length $rowCount")
    }
    /**
     * Returns the array element at the given [row] and [col].
     *
     * This method can be called using the index operator:
     * ```
     * value = matrix[row, col]
     * ```
     *
     * @throws IndexOutOfBoundsException If the [row] or [col] is out of bounds of this array.
     */
    operator fun get(row: Int, col: Int): T {
        checkBounds(row, col)
        return secretArray[getIndex(row, col)]
    }

    /**
     * Sets the array element at the given [row] and [col] to the given [value].
     *
     * This method can be called using the index operator:
     * ```
     * matrix[row, col] = value
     * ```
     *
     * @throws IndexOutOfBoundsException If the [row] or [col] is out of bounds of this array.
     */
    operator fun set(row: Int, col: Int, value: T) {
        checkBounds(row, col)
        secretArray[getIndex(row, col)] = value
    }

    /**
     * Returns the array of elements representing the given [row].
     *
     * @throws IndexOutOfBoundsException If the [row] is out of bounds of this array. TODO: check that this is true
     */
    @Suppress("UNCHECKED_CAST")
    fun getRow(row: Int): List<T> {
        return List(colCount) { get(row, it) }
    }

    /**
     * Returns the array of elements representing the given [col].
     *
     * @throws IndexOutOfBoundsException If the [col] is out of bounds of this array. TODO: check that this is true
     */
    @Suppress("UNCHECKED_CAST")
    fun getCol(col: Int): List<T> {
        return List(rowCount) { get(it, col) }
    }

    fun setRow(row: Int, value: List<T>) {
        if (row < 0 || row >= rowCount) throw IndexOutOfBoundsException("Row index $row is out of bounds for length $rowCount")
        if (value.size != colCount) throw IllegalArgumentException("Values list size (${value.size}) must match column count ($colCount)")
        for(i in 0 until colCount) set(row, i, value[i])
    }

    fun setCol(col: Int, value: List<T>) {
        if (col < 0 || col >= colCount) throw IndexOutOfBoundsException("Column index $col is out of bounds for length $colCount")
        if (value.size != rowCount) throw IllegalArgumentException("Values list size (${value.size}) must match row count ($rowCount)")
        for(i in 0 until rowCount) set(i, col, value[i])
    }

    /**
     * Returns an element at the given index or null if the index is out of bounds of this array.
     */
    fun getOrNull(row: Int, col: Int): T? {
        if(col >= colCount) return null
        return secretArray.getOrNull(getIndex(row, col))
    }

    fun rows(): Iterator<List<T>> {
        return object : Iterator<List<T>> {
            var pos = 0
            override fun hasNext() = pos < rowCount
            override fun next() = getRow(pos++)
        }
    }

    fun cols(): Iterator<List<T>> {
        return object : Iterator<List<T>> {
            var pos = 0
            override fun hasNext() = pos < colCount
            override fun next() = getCol(pos++)
        }
    }

    fun transpose(): Matrix<T> {
        return Matrix(colCount, rowCount) {r, c -> get(c, r)}
    }

    fun <S, R> multiply(scalar: S, func: (T, S) -> R): Matrix<R> {
        return Matrix(rowCount, colCount) {r, c -> func(get(r, c), scalar)}
    }

    fun <O, R> add(matrix: Matrix<O>, func: (T, O) -> R): Matrix<R> {
        if(rowCount != matrix.rowCount || colCount != matrix.colCount) throw IllegalArgumentException("Matrices must be the same size")
        return Matrix(rowCount, colCount) {r, c -> func(get(r, c), matrix[r, c])}
    }

    fun <O, R, F> multiply(matrix: Matrix<O>, elementFunc: (T, O) -> R, collapseFunc: (List<R>) -> F): Matrix<F> {
        if(colCount != matrix.rowCount) throw IllegalArgumentException("Matrices must have compatible sizes for multiplication")
        return Matrix(rowCount, matrix.colCount) { r, c ->
            collapseFunc(
                getRow(r).zip(matrix.getCol(c)) { e1, e2 ->
                    elementFunc(e1, e2)
                }
            )
        }
    }

    fun flatten(): Array<T> = secretArray.clone()

    fun sliceRows(rows: IntRange): Matrix<T> {
        return Matrix(rows.count(), colCount) {r, c -> get(rows.first + r,c)}
    }

    fun sliceCols(cols: IntRange): Matrix<T> {
        return Matrix(rowCount, cols.count()) {r, c -> get(r,cols.first + c)}
    }

    /**
     * Creates a string from all the rows seperated using [rowSeparator] and using the given [rowPrefix] and [rowPostfix] if supplied.
     *
     * Each row is created as a string by combining all the elements using [colSeparator]
     *
     * If the collection could be huge, you can specify a non-negative value of [totalLimit], in which case only the first [totalLimit]
     * elements will be appended, followed by the [truncated] string (which defaults to "...").
     * You can also use [rowLimit] to only print a certain amount of rows
     */
    fun joinToString(
        rowSeparator: CharSequence,
        colSeparator: CharSequence,
        rowPrefix: CharSequence = "",
        rowPostfix: CharSequence = "",
        rowLimit: Int = -1,
        totalLimit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((T) -> CharSequence)? = null
    ): String {
        val effectiveRowLimit = if (rowLimit < 0) rowCount else minOf(rowLimit, rowCount)

        val rowStrings = rows()
            .asSequence()
            .take(effectiveRowLimit)
            .map { row ->
                "" + rowPrefix + row.joinToString(
                    separator = colSeparator,
                    transform = transform,
                    limit = if (totalLimit < 0) -1 else totalLimit / effectiveRowLimit,
                    truncated = truncated
                ) + rowPostfix
            }
            .toList()

        return rowStrings.joinToString(rowSeparator)
    }

    val size: Int get() = rowCount * colCount

    operator fun contains(element: T): Boolean = secretArray.contains(element)

    override fun iterator(): Iterator<T> = secretArray.iterator()

    public override fun clone(): Matrix<T> {
        return Matrix(rowCount, colCount, secretArray = secretArray.clone())
    }

    override fun toString(): String{
        return joinToString(rowSeparator = "\n", colSeparator = ", ")
    }

}