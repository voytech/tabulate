package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.TableAttribute
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributeOperation

/**
 * Table attribute operation associated with table rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class TableAttributeRenderOperation<CTX : RenderingContext, ATTR : TableAttribute<*>, AC : TableContext> :
    AttributeOperation<CTX, TableAttribute<*>, ATTR, AC>

/**
 * Row attribute operation associated with row rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class RowAttributeRenderOperation<CTX : RenderingContext, ATTR : RowAttribute<*>, AC : RowContext> :
    AttributeOperation<CTX, RowAttribute<*>, ATTR, AC>


/**
 * Cell attribute operation associated with cell rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class CellAttributeRenderOperation<CTX : RenderingContext, ATTR : CellAttribute<*>> :
    AttributeOperation<CTX, CellAttribute<*>, ATTR, CellContext>

/**
 * Column attribute operation associated with column rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class ColumnAttributeRenderOperation<CTX : RenderingContext, ATTR : ColumnAttribute<*>, AC : ColumnContext> :
    AttributeOperation<CTX, ColumnAttribute<*>, ATTR, AC>

