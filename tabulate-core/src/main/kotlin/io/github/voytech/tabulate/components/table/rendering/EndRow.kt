package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.ColumnKey
import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutBoundaryType
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.operation.RenderingStatus
import io.github.voytech.tabulate.core.operation.VoidOperation
import io.github.voytech.tabulate.core.operation.hasLayoutEffect

fun interface EndRowOperation<CTX : RenderingContext, T: Any>: VoidOperation<CTX, RowEndRenderable<T>>

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
    
    override val boundaryToFit: LayoutBoundaryType = LayoutBoundaryType.INNER

    fun getCells(): Map<ColumnKey<T>, CellRenderable> = rowCellValues

    override fun defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            width = getMeasuredContentSize()?.width,
            height = getCurrentRowHeight(getRow(),1, uom),
            boundaryToFit
        )
    }

    override fun applyBoundingBox(bbox: RenderableBoundingBox, layout: TableLayout, status: RenderingStatus): Unit =
        with(layout) {
            if (!status.hasLayoutEffect()) return
            bbox.height?.let {
                val ops = SizingOptions.SET_LOCKED.takeIf { hasModelAttribute<HeightAttribute>() } ?: SizingOptions.SET
                setRowHeight(getRow(), it, ops)
            }
        }

    override fun toString(): String {
        return "RowEndRenderable(rowIndex=$rowIndex)"
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