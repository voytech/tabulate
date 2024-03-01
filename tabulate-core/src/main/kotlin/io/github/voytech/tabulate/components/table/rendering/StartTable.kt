package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutBoundaryType
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.VoidOperation

fun interface StartTableOperation<CTX : RenderingContext>: VoidOperation<CTX, TableStartRenderable>

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class TableContext(
    attributes: Attributes?,
) : Renderable<TableLayout>(attributes) {

    override val boundaryToFit = LayoutBoundaryType.OUTER

    override fun LayoutSpace.defineBoundingBox(layout: TableLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = leftTop.x,
            y = leftTop.y,
            width = getMeasuredSize()?.width,
            height = getMeasuredSize()?.height,
            boundaryToFit
        )
    }
}

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