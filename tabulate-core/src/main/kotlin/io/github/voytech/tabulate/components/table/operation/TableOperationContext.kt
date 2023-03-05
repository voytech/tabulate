package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.model.attributes.cell.TypeHintAttribute
import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.Context
import io.github.voytech.tabulate.core.template.operation.HasValue
import io.github.voytech.tabulate.core.template.operation.RenderableContext

/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */


fun Context.getSheetName(): String {
    return (getCustomAttributes()?.get("_sheetName") ?: error("")) as String
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

interface RowLayoutElement : RowCoordinate, LayoutElement<TableLayoutPolicy>, LayoutElementApply<TableLayoutPolicy> {
    override fun Layout.computeBoundingBox(policy: TableLayoutPolicy): LayoutElementBoundingBox = with(policy) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(0, uom),
            y = getAbsoluteRowPosition(getRow(), uom),
            width = getLayoutBoundary().getWidth().switchUnitOfMeasure(uom),
        )
    }

    override fun Layout.applyBoundingBox(context: LayoutElementBoundingBox, policy: TableLayoutPolicy): Unit =
        with(policy) {
            markHeightForMeasure(getRow(),context.flags.shouldMeasureHeight)
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

interface ColumnLayoutElement : ColumnCoordinate, LayoutElement<TableLayoutPolicy>,
    LayoutElementApply<TableLayoutPolicy> {
    override fun Layout.computeBoundingBox(policy: TableLayoutPolicy): LayoutElementBoundingBox = with(policy) {
        elementBoundingBox(x = getAbsoluteColumnPosition(getColumn(),uom), y = getAbsoluteRowPosition(0, uom))
    }

    override fun Layout.applyBoundingBox(context: LayoutElementBoundingBox, policy: TableLayoutPolicy): Unit =
        with(policy) {
            markWidthForMeasure(getColumn(),context.flags.shouldMeasureWidth)
            context.width?.let { setColumnWidth(getColumn(), it) }
        }
}

/**
 * Row and column coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface RowCellCoordinate : RowCoordinate, ColumnCoordinate

interface RowCellLayoutElement : RowCellCoordinate, LayoutElement<TableLayoutPolicy>,
    LayoutElementApply<TableLayoutPolicy> {

    override fun Layout.applyBoundingBox(context: LayoutElementBoundingBox, policy: TableLayoutPolicy): Unit =
        with(policy) {
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

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class TableContext(
    attributes: Attributes?,
) : AttributedContext(attributes)

/**
 * Column operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class ColumnContext(
    attributes: Attributes?,
) : RenderableContext<TableLayoutPolicy>(attributes)

/**
 * Row operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class RowContext(
    attributes: Attributes?,
) : RenderableContext<TableLayoutPolicy>(attributes)

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableStart(
    attributes: Attributes?,
) : TableContext(attributes)

internal fun <T : Any> Table<T>.asTableStart(customAttributes: MutableMap<String, Any>): TableStart =
    TableStart(attributes?.forContext<TableStart>()).apply { additionalAttributes = customAttributes }

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableEnd(
    attributes: Attributes?,
) : TableContext(attributes)

internal fun <T : Any> Table<T>.asTableEnd(customAttributes: MutableMap<String, Any>): TableEnd =
    TableEnd(attributes?.forContext<TableEnd>()).apply { additionalAttributes = customAttributes }

/**
 * Row operation context with additional model attributes applicable on row level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowStart(
    attributes: Attributes?,
    val rowIndex: Int,
) : RowContext(attributes), RowLayoutElement {
    override fun getRow(): Int = rowIndex
}

internal fun <T : Any> SyntheticRow<T>.createRowStart(
    rowIndex: Int,
    customAttributes: MutableMap<String, Any>,
): RowStart {
    return RowStart(rowIndex = table.getRowIndex(rowIndex), attributes = rowStartAttributes)
        .apply { additionalAttributes = customAttributes }
}

/**
 * Row operation context with additional model attributes applicable on row level.
 * Additionally, it contains also all resolved cell operation context for each contained cell.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowEnd<T>(
    attributes: Attributes?,
    val rowCellValues: Map<ColumnKey<T>, CellContext>,
    val rowIndex: Int,
) : RowContext(attributes), RowLayoutElement {

    override fun getRow(): Int = rowIndex

    fun getCells(): Map<ColumnKey<T>, CellContext> = rowCellValues

    override fun Layout.computeBoundingBox(policy: TableLayoutPolicy): LayoutElementBoundingBox = with(policy) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(0, uom),
            y = getAbsoluteRowPosition(getRow(), uom),
            width = getLayoutBoundary().getWidth().switchUnitOfMeasure(uom),
            height = getRowHeight(getRow(),1, uom)
        )
    }
}

internal fun <T : Any> SyntheticRow<T>.createRowEnd(
    rowStart: RowStart,
    rowCellValues: Map<ColumnKey<T>, CellContext>,
): RowEnd<T> =
    RowEnd(
        rowIndex = rowStart.rowIndex,
        attributes = rowEndAttributes,
        rowCellValues = rowCellValues
    ).apply { additionalAttributes = rowStart.additionalAttributes }


/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ColumnStart(
    attributes: Attributes? = null,
    val columnIndex: Int,
) : ColumnContext(attributes), ColumnLayoutElement {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.BEFORE_FIRST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T : Any> ColumnDef<T>.asColumnStart(
    table: Table<T>,
    attributes: Attributes,
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
    attributes: Attributes? = null,
    val columnIndex: Int,
) : ColumnContext(attributes), ColumnLayoutElement {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.AFTER_LAST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T : Any> ColumnDef<T>.asColumnEnd(
    table: Table<T>,
    attributes: Attributes,
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
    val cellValue: CellValue,
    attributes: Attributes?,
    val rowIndex: Int,
    val columnIndex: Int,
    override val value: Any = cellValue.value,
) : RenderableContext<TableLayoutPolicy>(attributes), RowCellLayoutElement, HasValue<Any> {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex

    override fun Layout.computeBoundingBox(policy: TableLayoutPolicy): LayoutElementBoundingBox = with(policy) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(getColumn(), uom),
            y = getAbsoluteRowPosition(getRow(), uom),
            width = getColumnWidth(getColumn(), cellValue.colSpan, uom),
            height = getRowHeight(getRow(), cellValue.rowSpan, uom)
        )
    }
}

internal fun <T : Any> SyntheticRow<T>.createCellContext(
    row: SourceRow<T>,
    column: ColumnDef<T>,
    customAttributes: MutableMap<String, Any>,
): CellContext? =
    cellDefinitions.resolveCellValue(column, row)?.let { value ->
        CellContext(
            cellValue = value,
            attributes = cellContextAttributes[column],
            rowIndex = table.getRowIndex(row.rowIndexValue()),
            columnIndex = table.getColumnIndex(column.index)
        ).apply { additionalAttributes = customAttributes }
    }

fun CellContext.getTypeHint(): TypeHintAttribute? = getModelAttribute<TypeHintAttribute>()