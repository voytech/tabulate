package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.VoidOperation

fun interface EndTableOperation<CTX : RenderingContext>: VoidOperation<CTX, TableEndRenderableEntity>

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableEndRenderableEntity(
    attributes: Attributes?,
) : TableContext(attributes) {
    override fun toString(): String {
        return "TableEndRenderable(attributes=$attributes)"
    }
}

internal fun <T : Any> Table<T>.asTableEnd(customAttributes: StateAttributes): TableEndRenderableEntity =
    TableEndRenderableEntity(attributes?.forContext<TableEndRenderableEntity>()).apply { additionalAttributes = customAttributes.data }