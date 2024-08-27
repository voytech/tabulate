package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.model.attributes.cell.TypeHintAttribute
import io.github.voytech.tabulate.core.model.stateAttributes
import io.github.voytech.tabulate.core.operation.CustomAttributes

/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun CustomAttributes.getSheetName(): String =
    getCustomAttributes().stateAttributes().let { attributes ->
        attributes.getExecutionContext<PageExecutionContext>()
            ?.currentPageTitleWithNumber()
            ?: (getCustomAttributes()["_sheetName"] as? String
                ?: error("Cannot resolve sheet name"))
    }


/**
 * CellValue representing cell associated data exposed by row cell operation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class CellValue(
    val value: Any,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
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
    val tableName: String,
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

internal fun <T : Any> Table<T>.getRowIndex(rowIndex: Int) = (firstRow ?: 0) + rowIndex

internal fun <T : Any> Table<T>.getColumnIndex(columnIndex: Int) = (firstColumn ?: 0) + columnIndex

fun CellRenderableEntity.getTypeHint(): TypeHintAttribute? = getModelAttribute<TypeHintAttribute>()