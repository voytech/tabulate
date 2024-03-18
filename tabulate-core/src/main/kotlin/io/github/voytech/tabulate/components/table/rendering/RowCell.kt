package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.SourceRow
import io.github.voytech.tabulate.components.table.model.resolveCellValue
import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.operation.HasValue
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.Renderable

fun interface RenderRowCellOperation<CTX : RenderingContext> : Operation<CTX, CellRenderable>

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
) : Renderable<TableLayout>(attributes), LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout>, RowCellCoordinate,
    HasValue<Any> {

    override fun getRow(): Int = rowIndex

    override fun getColumn(): Int = columnIndex

    override val boundaryToFit: LayoutBoundaryType = LayoutBoundaryType.INNER

    override fun LayoutSpace.defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(getColumn()),
            y = getAbsoluteRowPosition(getRow()),
            width = getMeasuredColumnWidth(getColumn(), cellValue.colSpan, uom),
            height = getMeasuredRowHeight(getRow(), cellValue.rowSpan, uom),
            boundaryToFit
        )
    }

    override fun LayoutSpace.applyBoundingBox(bbox: RenderableBoundingBox, layout: TableLayout): Unit =
        with(layout) {
            bbox.width?.let {
                setColumnWidth(getColumn(), it, SizingOptions.SET_IF_GREATER)
            }
            bbox.height?.let {
                setRowHeight(getRow(), it, SizingOptions.SET_IF_GREATER)
            }
        }
}

internal fun <T : Any> SyntheticRow<T>.createCellContext(
    row: SourceRow<T>, column: ColumnDef<T>, customAttributes: MutableMap<String, Any>,
): CellRenderable? =
    cellDefinitions.resolveCellValue(column, row)?.let { value ->
        CellRenderable(
            cellValue = value,
            attributes = cellContextAttributes[column],
            rowIndex = table.getRowIndex(row.rowIndexValue()),
            columnIndex = table.getColumnIndex(column.index)
        ).apply { additionalAttributes = customAttributes }
    }
