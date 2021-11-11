package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.Table

sealed interface Context {
    fun getContextAttributes() : MutableMap<String, Any>?
}

sealed class ContextData : Context {
    internal var additionalAttributes: MutableMap<String, Any>? = null
    override fun getContextAttributes(): MutableMap<String, Any>? = additionalAttributes
}

fun Context.getTableId(): String {
    return (getContextAttributes()?.get("_tableId") ?: error("")) as String
}

data class CellValue(
        val value: Any,
        val colSpan: Int = 1,
        val rowSpan: Int = 1
)

interface RowCoordinate {
    fun getRow(): Int
}

interface ColumnCoordinate {
    fun getColumn(): Int
}

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

fun <T> Table<T>.getRowIndex(rowIndex: Int) = (firstRow ?: 0) + rowIndex

fun <T> Table<T>.getColumnIndex(columnIndex: Int) = (firstColumn ?: 0) + columnIndex