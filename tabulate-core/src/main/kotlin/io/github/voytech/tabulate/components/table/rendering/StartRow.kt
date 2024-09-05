package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.operation.RenderableEntity
import io.github.voytech.tabulate.core.operation.RenderingStatus
import io.github.voytech.tabulate.core.operation.VoidOperation
import io.github.voytech.tabulate.core.operation.hasLayoutEffect

fun interface StartRowOperation<CTX : RenderingContext> : VoidOperation<CTX, RowStartRenderableEntity>

interface RowLayoutElement : RowCoordinate, LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout> {
    override fun TableLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            width = getMeasuredContentSize()?.width,
            height = null,
            boundaryToFit
        )


}

/**
 * Row operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class RowRenderableEntity(
    attributes: Attributes?,
) : RenderableEntity<TableLayout>(attributes)

/**
 * Row operation context with additional model attributes applicable on row level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowStartRenderableEntity(
    attributes: Attributes?,
    val rowIndex: Int,
) : RowRenderableEntity(attributes), RowLayoutElement {
    override fun getRow(): Int = rowIndex
    override val boundaryToFit: BoundaryType = BoundaryType.CONTENT

    override fun TableLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(0),
            y = getAbsoluteRowPosition(getRow()),
            height = getModelAttribute<HeightAttribute>()?.value,
            width = null,
            type = boundaryToFit
        )


    override fun TableLayout.applyMeasures(bbox: RenderableBoundingBox, status: RenderingStatus) {
        if (!status.hasLayoutEffect()) return
        startRowSizing(getRow())
        bbox.height?.let {
            val ops = SizingOptions.SET_LOCKED.takeIf { hasModelAttribute<HeightAttribute>() } ?: SizingOptions.SET
            setRowHeight(getRow(), it, ops)
        }
    }

    override fun toString(): String {
        return "RowStartRenderable(rowIndex=$rowIndex)"
    }

}

internal fun <T : Any> SyntheticRow<T>.createRowStart(
    rowIndex: Int,
    customAttributes: MutableMap<String, Any>,
): RowStartRenderableEntity {
    return RowStartRenderableEntity(rowIndex = table.getRowIndex(rowIndex), attributes = rowStartAttributes)
        .apply { additionalAttributes = customAttributes }
}