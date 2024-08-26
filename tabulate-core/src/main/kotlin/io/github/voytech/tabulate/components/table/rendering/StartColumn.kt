package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.RenderingStatus
import io.github.voytech.tabulate.core.operation.VoidOperation
import io.github.voytech.tabulate.core.operation.hasLayoutEffect

fun interface StartColumnOperation<CTX : RenderingContext> : VoidOperation<CTX, ColumnStartRenderable>


/**
 * Column operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class ColumnRenderable(
    attributes: Attributes?,
) : Renderable<TableLayout>(attributes)


/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ColumnStartRenderable(
    attributes: Attributes? = null,
    val columnIndex: Int,
) : ColumnRenderable(attributes), LayoutElement<TableLayout>, ApplyLayoutElement<TableLayout>, ColumnCoordinate {

    override val boundaryToFit: LayoutBoundaryType = LayoutBoundaryType.INNER

    override fun defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = getAbsoluteColumnPosition(getColumn()),
            y = getAbsoluteRowPosition(0),
            width = getModelAttribute<WidthAttribute>()?.value,
            height = null,
            boundaryToFit
        )
    }

    override fun applyBoundingBox(bbox: RenderableBoundingBox, layout: TableLayout, status: RenderingStatus): Unit =
        with(layout) {
            if (!status.hasLayoutEffect()) return
            bbox.width?.let {
                val ops = SizingOptions.SET_LOCKED.takeIf { hasModelAttribute<WidthAttribute>() } ?: SizingOptions.SET
                setColumnWidth(getColumn(), it, ops)
            }
        }

    override fun getColumn(): Int = columnIndex

    override fun toString(): String {
        return "ColumnStartRenderable(columnIndex=$columnIndex)"
    }
}

internal fun <T : Any> ColumnDef<T>.asColumnStart(
    table: Table<T>,
    attributes: Attributes,
    customAttributes: StateAttributes,
) = ColumnStartRenderable(
    columnIndex = table.getColumnIndex(index),
    attributes = attributes
).apply { additionalAttributes = customAttributes.data }
