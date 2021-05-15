package io.github.voytech.tabulate.template.context

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
