package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.RenderingStatus
import io.github.voytech.tabulate.core.operation.VoidOperation
import io.github.voytech.tabulate.core.operation.hasLayoutEffect

fun interface StartRowOperation<CTX : RenderingContext> : VoidOperation<CTX, RowStartRenderable>

interface RowLayoutElement : RowCoordinate, LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout> {
    override fun Region.defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            width = getMeasuredContentSize()?.width,
            height = null,
            boundaryToFit
        )
    }


}

/**
 * Row operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class RowRenderable(
    attributes: Attributes?,
) : Renderable<TableLayout>(attributes)

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
    override val boundaryToFit: LayoutBoundaryType = LayoutBoundaryType.INNER

    override fun Region.defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            height = getModelAttribute<HeightAttribute>()?.value,
            width = null,
            type = boundaryToFit
        )
    }

    override fun Region.applyBoundingBox(bbox: RenderableBoundingBox, layout: TableLayout, status: RenderingStatus): Unit =
        with(layout) {
            if (!status.hasLayoutEffect()) return
            bbox.height?.let {
                val ops = SizingOptions.SET_LOCKED.takeIf { hasModelAttribute<HeightAttribute>() } ?: SizingOptions.SET
                setRowHeight(getRow(), it, ops)
            }
        }

}

internal fun <T : Any> SyntheticRow<T>.createRowStart(
    rowIndex: Int,
    customAttributes: MutableMap<String, Any>,
): RowStartRenderable {
    return RowStartRenderable(rowIndex = table.getRowIndex(rowIndex), attributes = rowStartAttributes)
        .apply { additionalAttributes = customAttributes }
}