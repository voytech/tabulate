package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.Table

/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed interface Context {
    fun getContextAttributes() : MutableMap<String, Any>?
}

/**
 * Basic implementation of [Context] interface
 * @see Context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed class ContextData : Context {
    internal var additionalAttributes: MutableMap<String, Any>? = null
    override fun getContextAttributes(): MutableMap<String, Any>? = additionalAttributes
}

fun Context.getTableId(): String {
    return (getContextAttributes()?.get("_tableId") ?: error("")) as String
}

/**
 * CellValue representing cell associated data exposed by row cell operation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class CellValue(
        val value: Any,
        val colSpan: Int = 1,
        val rowSpan: Int = 1
)

/**
 * Row coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface RowCoordinate {
    fun getRow(): Int
}

/**
 * Column coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface ColumnCoordinate {
    fun getColumn(): Int
}

/**
 * Row and column coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface RowCellCoordinate : RowCoordinate, ColumnCoordinate

data class Coordinates(
        val tableName: String
) {
    var rowIndex: Int = 0
        internal set
    var columnIndex: Int = 0
        internal set

    constructor(tableName: String, rowIdx: Int, columnIdx: Int) : this(tableName) {
        rowIndex = rowIdx
        columnIndex = columnIdx
    }
}

internal fun <T> Table<T>.getRowIndex(rowIndex: Int) = (firstRow ?: 0) + rowIndex

internal fun <T> Table<T>.getColumnIndex(columnIndex: Int) = (firstColumn ?: 0) + columnIndex