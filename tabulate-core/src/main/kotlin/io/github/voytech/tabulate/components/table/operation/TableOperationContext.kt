package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.TypeHintAttribute
import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.components.table.template.TableTemplate
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.model.orEmpty
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.Context

/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */


fun Context.getSheetName(): String {
    return (getContextAttributes()?.get("_sheetName") ?: error("")) as String
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

interface RowLayoutElement : RowCoordinate, LayoutElement, LayoutElementApply {
    override fun Layout.computeBoundaries(): LayoutElementBoundaries = query.elementBoundaries(
        y = query.getY(getRow().asYPosition(), uom)
    )

    override fun Layout.applyBoundaries(context: LayoutElementBoundaries): Unit = with(query as TableLayoutQueries) {
        context.height?.let { setRowHeight(getRow(), it) }
    }
}

/**
 * Column coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface ColumnCoordinate {
    fun getColumn(): Int
}

interface ColumnLayoutElement : ColumnCoordinate, LayoutElement, LayoutElementApply {
    override fun Layout.computeBoundaries(): LayoutElementBoundaries = query.elementBoundaries(
        x = query.getX(getColumn().asXPosition(), uom),
    )

    override fun Layout.applyBoundaries(context: LayoutElementBoundaries): Unit = with(query as TableLayoutQueries) {
        context.width?.let { setColumnWidth(getColumn(), it) }
    }
}

/**
 * Row and column coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface RowCellCoordinate : RowCoordinate, ColumnCoordinate

interface RowCellLayoutElement : RowCellCoordinate, LayoutElement, LayoutElementApply {
    override fun Layout.computeBoundaries(): LayoutElementBoundaries = query.elementBoundaries(
        x = query.getX(getColumn().asXPosition(), uom),
        y = query.getY(getRow().asYPosition(), uom)
    )

    override fun Layout.applyBoundaries(context: LayoutElementBoundaries): Unit = with(query as TableLayoutQueries) {
        context.width?.let { setColumnWidth(getColumn(), it) }
        context.height?.let { setRowHeight(getRow(), it) }
    }
}

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

@JvmName("getCellModelAttributes")
inline fun <reified T : CellAttribute<T>> AttributedContext<*>.getModelAttribute(): T? =
    getModelAttribute(T::class.java)


/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class TableContext<C: TableContext<C>>(
    attributes: Attributes<C>?,
) : AttributedContext<C>(attributes)

/**
 * Column operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class ColumnContext<C: ColumnContext<C>>(
    attributes: Attributes<C>?,
) : AttributedContext<C>(attributes)

/**
 * Row operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class RowContext<C: RowContext<C>>(
    attributes: Attributes<C>?,
) : AttributedContext<C>(attributes)

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableStart(
    attributes: Attributes<TableStart>?,
) : TableContext<TableStart>(attributes)

internal fun <T : Any> Table<T>.asTableStart(customAttributes: MutableMap<String, Any>): TableStart =
    TableStart(attributes?.forContext()).apply { additionalAttributes = customAttributes }

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableEnd(
    attributes: Attributes<TableEnd>?,
) : TableContext<TableEnd>(attributes)

internal fun <T : Any> Table<T>.asTableEnd(customAttributes: MutableMap<String, Any>): TableEnd =
    TableEnd(attributes?.forContext()).apply { additionalAttributes = customAttributes }

/**
 * Row operation context with additional model attributes applicable on row level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowStart(
    attributes: Attributes<RowStart>?,
    val rowIndex: Int,
) : RowContext<RowStart>(attributes), RowLayoutElement {
    override fun getRow(): Int = rowIndex
}

internal fun <T : Any> SyntheticRow<T>.asRowStart(
    rowIndex: Int,
    customAttributes: MutableMap<String, Any>,
): RowStart {
    return RowStart(rowIndex = table.getRowIndex(rowIndex), attributes = rowStartAttributes)
        .apply { additionalAttributes = customAttributes }
}

/**
 * Row operation context with additional model attributes applicable on row level.
 * Additionally it contains also all resolved cell operation context for each contained cell.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowEnd<T>(
    attributes: Attributes<RowEnd<T>>?,
    val rowCellValues: Map<ColumnKey<T>, CellContext>,
    val rowIndex: Int,
) : RowContext<RowEnd<T>>(attributes), RowLayoutElement {
    override fun getRow(): Int = rowIndex

    fun getCells(): Map<ColumnKey<T>, CellContext> = rowCellValues

}

internal fun  <T : Any> SyntheticRow<T>.asRowEnd(rowStart: RowStart,rowCellValues: Map<ColumnKey<T>, CellContext>): RowEnd<T> =
    RowEnd(
        rowIndex = rowStart.rowIndex,
        attributes =  rowEndAttributes,
        rowCellValues = rowCellValues
    ).apply { additionalAttributes = rowStart.additionalAttributes }


/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ColumnStart(
    attributes: Attributes<ColumnStart>? = null,
    val columnIndex: Int,
) : ColumnContext<ColumnStart>(attributes), ColumnLayoutElement {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.BEFORE_FIRST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T : Any> ColumnDef<T>.asColumnStart(
    table: Table<T>,
    attributes: Attributes<ColumnStart>,
    customAttributes: MutableMap<String, Any>,
) = ColumnStart(
    columnIndex = table.getColumnIndex(index),
    attributes = attributes
).apply { additionalAttributes = customAttributes }

/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

class ColumnEnd(
    attributes: Attributes<ColumnEnd>? = null,
    val columnIndex: Int,
) : ColumnContext<ColumnEnd>(attributes), ColumnLayoutElement {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.AFTER_LAST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T : Any> ColumnDef<T>.asColumnEnd(
    table: Table<T>,
    attributes: Attributes<ColumnEnd>,
    customAttributes: MutableMap<String, Any>,
) = ColumnEnd(
    columnIndex = table.getColumnIndex(index),
    attributes = attributes
).apply { additionalAttributes = customAttributes }

/**
 * Cell operation context with additional model attributes applicable on cell level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellContext(
    val value: CellValue,
    attributes: Attributes<CellContext>?,
    val rowIndex: Int,
    val columnIndex: Int,
    val rawValue: Any = value.value,
) : AttributedContext<CellContext>(attributes), RowCellLayoutElement {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}

internal fun <T : Any> SyntheticRow<T>.asCellContext(
    row: SourceRow<T>,
    column: ColumnDef<T>,
    customAttributes: MutableMap<String, Any>,
): CellContext? =
    cellDefinitions.resolveCellValue(column, row)?.let { value ->
        CellContext(
            value = value,
            attributes = cellContextAttributes[column],
            rowIndex = table.getRowIndex(row.rowIndexValue()),
            columnIndex = table.getColumnIndex(column.index)
        ).apply { additionalAttributes = customAttributes }
    }

fun CellContext.getTypeHint(): TypeHintAttribute? = getModelAttribute<TypeHintAttribute>()