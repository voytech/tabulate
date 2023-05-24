package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.model.attributes.cell.TypeHintAttribute
import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.LayoutElement
import io.github.voytech.tabulate.core.layout.ApplyLayoutElement
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.policy.TableLayout
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.operation.Context
import io.github.voytech.tabulate.core.operation.HasValue
import io.github.voytech.tabulate.core.operation.Renderable

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

interface RowLayoutElement : RowCoordinate, LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout> {
    override fun LayoutSpace.defineBoundingBox(policy: TableLayout): RenderableBoundingBox = with(policy) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            width = getBoundingRectangle().getWidth().switchUnitOfMeasure(uom),
        )
    }

    override fun LayoutSpace.applyBoundingBox(context: RenderableBoundingBox, policy: TableLayout): Unit =
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

interface ColumnLayoutElement : ColumnCoordinate, LayoutElement<TableLayout>,
    ApplyLayoutElement<TableLayout> {
    override fun LayoutSpace.defineBoundingBox(policy: TableLayout): RenderableBoundingBox = with(policy) {
        elementBoundingBox(x = getAbsoluteColumnPosition(getColumn()), y = getAbsoluteRowPosition(0))
    }

    override fun LayoutSpace.applyBoundingBox(context: RenderableBoundingBox, policy: TableLayout): Unit =
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

interface RowLayoutElementCell : RowCellCoordinate, LayoutElement<TableLayout>,
    ApplyLayoutElement<TableLayout> {

    override fun LayoutSpace.applyBoundingBox(context: RenderableBoundingBox, policy: TableLayout): Unit =
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
) : Renderable<TableLayout>(attributes) {
    override fun LayoutSpace.defineBoundingBox(policy: TableLayout): RenderableBoundingBox = with(policy) {
        elementBoundingBox(
            x = getX(0.asXPosition(), uom),
            y = getY(0.asYPosition(), uom),
            width = policy.getMeasuredSize()?.width,
            height = policy.getMeasuredSize()?.height
        )
    }

}

/**
 * Column operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class ColumnRenderable(
    attributes: Attributes?,
) : Renderable<TableLayout>(attributes)

/**
 * Row operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class RowRenderable(
    attributes: Attributes?,
) : Renderable<TableLayout>(attributes)

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableStartRenderable(
    attributes: Attributes?,
) : TableContext(attributes)

internal fun <T : Any> Table<T>.asTableStart(customAttributes: StateAttributes): TableStartRenderable =
    TableStartRenderable(attributes?.forContext<TableStartRenderable>()).apply { additionalAttributes = customAttributes.data }

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableEndRenderable(
    attributes: Attributes?,
) : TableContext(attributes)

internal fun <T : Any> Table<T>.asTableEnd(customAttributes: StateAttributes): TableEndRenderable =
    TableEndRenderable(attributes?.forContext<TableEndRenderable>()).apply { additionalAttributes = customAttributes.data }

/**
 * Row operation context with additional model attributes applicable on row level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowStartRenderable(
    attributes: Attributes?,
    val rowIndex: Int,
) : RowRenderable(attributes), RowLayoutElement {
    override fun getRow(): Int = rowIndex
}

internal fun <T : Any> SyntheticRow<T>.createRowStart(
    rowIndex: Int,
    customAttributes: MutableMap<String, Any>,
): RowStartRenderable {
    return RowStartRenderable(rowIndex = table.getRowIndex(rowIndex), attributes = rowStartAttributes)
        .apply { additionalAttributes = customAttributes }
}

/**
 * Row operation context with additional model attributes applicable on row level.
 * Additionally, it contains also all resolved cell operation context for each contained cell.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowEndRenderable<T>(
    attributes: Attributes?,
    val rowCellValues: Map<ColumnKey<T>, CellRenderable>,
    val rowIndex: Int,
) : RowRenderable(attributes), RowLayoutElement {

    override fun getRow(): Int = rowIndex

    fun getCells(): Map<ColumnKey<T>, CellRenderable> = rowCellValues

    override fun LayoutSpace.defineBoundingBox(policy: TableLayout): RenderableBoundingBox = with(policy) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            width = getBoundingRectangle().getWidth().switchUnitOfMeasure(uom),
            height = getRowHeight(getRow(),1, uom)
        )
    }
}

internal fun <T : Any> SyntheticRow<T>.createRowEnd(
    rowStart: RowStartRenderable,
    rowCellValues: Map<ColumnKey<T>, CellRenderable>,
): RowEndRenderable<T> =
    RowEndRenderable(
        rowIndex = rowStart.rowIndex,
        attributes = rowEndAttributes,
        rowCellValues = rowCellValues
    ).apply { additionalAttributes = rowStart.additionalAttributes }


/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ColumnStartRenderable(
    attributes: Attributes? = null,
    val columnIndex: Int,
) : ColumnRenderable(attributes), ColumnLayoutElement {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.BEFORE_FIRST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T : Any> ColumnDef<T>.asColumnStart(
    table: Table<T>,
    attributes: Attributes,
    customAttributes: StateAttributes,
) = ColumnStartRenderable(
    columnIndex = table.getColumnIndex(index),
    attributes = attributes
).apply { additionalAttributes = customAttributes.data }

/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

class ColumnEndRenderable(
    attributes: Attributes? = null,
    val columnIndex: Int,
) : ColumnRenderable(attributes), ColumnLayoutElement {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.AFTER_LAST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T : Any> ColumnDef<T>.asColumnEnd(
    table: Table<T>,
    attributes: Attributes,
    customAttributes: StateAttributes,
) = ColumnEndRenderable(
    columnIndex = table.getColumnIndex(index),
    attributes = attributes
).apply { additionalAttributes = customAttributes.data }

/**
 * Cell operation context with additional model attributes applicable on cell level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellRenderable(
    val cellValue: CellValue,
    attributes: Attributes?,
    val rowIndex: Int,
    val columnIndex: Int,
    override val value: Any = cellValue.value,
) : Renderable<TableLayout>(attributes), RowLayoutElementCell, HasValue<Any> {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex

    override fun LayoutSpace.defineBoundingBox(policy: TableLayout): RenderableBoundingBox = with(policy) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(getColumn()),
            y = getAbsoluteRowPosition(getRow()),
            width = getColumnWidth(getColumn(), cellValue.colSpan, uom),
            height = getRowHeight(getRow(), cellValue.rowSpan, uom)
        )
    }
}

internal fun <T : Any> SyntheticRow<T>.createCellContext(
    row: SourceRow<T>,
    column: ColumnDef<T>,
    customAttributes: MutableMap<String, Any>,
): CellRenderable? =
    cellDefinitions.resolveCellValue(column, row)?.let { value ->
        CellRenderable(
            cellValue = value,
            attributes = cellContextAttributes[column],
            rowIndex = table.getRowIndex(row.rowIndexValue()),
            columnIndex = table.getColumnIndex(column.index)
        ).apply { additionalAttributes = customAttributes }
    }

fun CellRenderable.getTypeHint(): TypeHintAttribute? = getModelAttribute<TypeHintAttribute>()