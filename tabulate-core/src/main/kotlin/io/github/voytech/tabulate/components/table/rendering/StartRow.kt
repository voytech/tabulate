package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.ApplyLayoutElement
import io.github.voytech.tabulate.core.layout.LayoutElement
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.VoidOperation

fun interface StartRowOperation<CTX : RenderingContext> : VoidOperation<CTX, RowStartRenderable>

interface RowLayoutElement : RowCoordinate, LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout> {
    override fun LayoutSpace.defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            width = getMeasuredContentSize()?.width
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

    override fun LayoutSpace.defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            height = getModelAttribute<HeightAttribute>()?.value,
        )
    }

    override fun LayoutSpace.applyBoundingBox(context: RenderableBoundingBox, layout: TableLayout): Unit =
        with(layout) {
            context.height?.let {
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