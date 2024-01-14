package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.ApplyLayoutElement
import io.github.voytech.tabulate.core.layout.LayoutElement
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.VoidOperation

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

    override fun LayoutSpace.defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        elementBoundingBox(
            x = getAbsoluteColumnPosition(getColumn()),
            y = getAbsoluteRowPosition(0),
            width = getModelAttribute<WidthAttribute>()?.value
        )
    }

    override fun LayoutSpace.applyBoundingBox(context: RenderableBoundingBox, layout: TableLayout): Unit =
        with(layout) {
            context.width?.let {
                val ops = SizingOptions.SET_LOCKED.takeIf { hasModelAttribute<WidthAttribute>() } ?: SizingOptions.SET
                setColumnWidth(getColumn(), it, ops)
            }
        }

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
