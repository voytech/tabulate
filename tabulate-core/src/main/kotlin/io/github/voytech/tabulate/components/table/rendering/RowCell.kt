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
import io.github.voytech.tabulate.core.operation.*

fun interface RenderRowCellOperation<CTX : RenderingContext> : Operation<CTX, CellRenderableEntity>

/**
 * Cell operation context with additional model attributes applicable on cell level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellRenderableEntity(
    val cellValue: CellValue,
    attributes: Attributes?,
    val rowIndex: Int,
    val columnIndex: Int,
    override val value: Any = cellValue.value,
) : RenderableEntity<TableLayout>(attributes), LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout>, RowCellCoordinate,
    HasValue<Any> {

    override fun getRow(): Int = rowIndex

    override fun getColumn(): Int = columnIndex

    override val boundaryToFit: BoundaryType = BoundaryType.CONTENT

    override fun TableLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(getColumn()),
            y = getAbsoluteRowPosition(getRow()),
            width = getMeasuredColumnWidth(getColumn(), cellValue.colSpan, uom),
            height = getMeasuredRowHeight(getRow(), cellValue.rowSpan, uom),
            boundaryToFit
        )

    override fun TableLayout.absorbRenderableBoundingBox(bbox: RenderableBoundingBox, status: RenderingStatus) {
        if (!status.hasLayoutEffect()) return
        bbox.width?.let {
            setColumnWidth(getColumn(), it, SizingOptions.SET_IF_GREATER)
        }
        bbox.height?.let {
            setRowHeight(getRow(), it, SizingOptions.SET_IF_GREATER)
        }
    }

    override fun toString(): String {
        return "RowCellRenderable(rowIndex=$rowIndex, columnIndex=$columnIndex, cellValue=$cellValue)"
    }
}

internal fun <T : Any> SyntheticRow<T>.createCellContext(
    row: SourceRow<T>, column: ColumnDef<T>, customAttributes: MutableMap<String, Any>,
): CellRenderableEntity? =
    cellDefinitions.resolveCellValue(column, row)?.let { value ->
        CellRenderableEntity(
            cellValue = value,
            attributes = cellContextAttributes[column],
            rowIndex = table.getRowIndex(row.rowIndexValue()),
            columnIndex = table.getColumnIndex(column.index)
        ).apply { additionalAttributes = customAttributes }
    }
