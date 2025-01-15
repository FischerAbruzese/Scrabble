package util

class Matrix<T>(private val rowCount: Int, private val colCount: Int, init: (Int, Int) -> T) : Iterable<T>{

    @Suppress("UNCHECKED_CAST")
    private val secretArray = Array<Any?>(rowCount * colCount) { i ->
        val (row, col) = getRowCol(i)
        init(row, col)
    } as Array<T>
    private fun getRowCol(index: Int) = index / colCount to index % colCount
    private fun getIndex(row: Int, col: Int) = row*colCount + col


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
    operator fun get(row: Int, col: Int) = secretArray[getIndex(row, col)]

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
    operator fun set(row: Int, col: Int, value: T) { secretArray[getIndex(row, col)] = value }

    /**
     * Returns the array of elements representing the given [row].
     *
     * @throws IndexOutOfBoundsException If the [row] is out of bounds of this array. TODO: check that this is true
     */
    @Suppress("UNCHECKED_CAST")
    fun getRow(row: Int): Array<T> {
        return Array<Any?>(colCount) {get(row, it)} as Array<T>
    }

    /**
     * Returns the array of elements representing the given [col].
     *
     * @throws IndexOutOfBoundsException If the [col] is out of bounds of this array. TODO: check that this is true
     */
    @Suppress("UNCHECKED_CAST")
    fun getCol(col: Int): Array<T> {
        return Array<Any?>(rowCount) {get(it, col)} as Array<T>
    }

    fun setRow(row: Int, value: Array<T>) {
        for(i in 0..colCount) set(row, i, value[i])
    }

    fun setCol(col: Int, value: Array<T>) {
        for(i in 0..rowCount) set(i, col, value[i])
    }

    /**
     * Returns an element at the given index or null if the index is out of bounds of this array.
     */
    fun getOrNull(row: Int, col: Int) = secretArray.getOrNull(getIndex(row, col))

    fun rows(): Iterator<Array<T>> {
        return object : Iterator<Array<T>> {
            var pos = 0
            override fun hasNext() = pos < rowCount
            override fun next() = getRow(pos++)
        }
    }

    fun cols(): Iterator<Array<T>> {
        return object : Iterator<Array<T>> {
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
        if(rowCount != matrix.colCount || colCount != matrix.rowCount) throw IllegalArgumentException("Matrices must have inverted sizes")
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
        return Matrix(rows.count(), colCount) {r, c -> get(r,c)}
    }

    fun sliceCols(cols: IntRange): Matrix<T> {
        return Matrix(rowCount, cols.count()) {r, c -> get(r,c)}
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

    override fun toString(): String{
        return joinToString(rowSeparator = "\n", colSeparator = ", ")
    }

}