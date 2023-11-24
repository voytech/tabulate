package io.github.voytech.tabulate.components.table.rendering

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.VoidOperation

fun interface EndTableOperation<CTX : RenderingContext>: VoidOperation<CTX, TableEndRenderable>

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableEndRenderable(
    attributes: Attributes?,
) : TableContext(attributes)

internal fun <T : Any> Table<T>.asTableEnd(customAttributes: StateAttributes): TableEndRenderable =
    TableEndRenderable(attributes?.forContext<TableEndRenderable>()).apply { additionalAttributes = customAttributes.data }