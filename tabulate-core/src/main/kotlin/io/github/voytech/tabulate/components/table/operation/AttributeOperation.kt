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
abstract class TableAttributeRenderOperation<CTX : RenderingContext, A : TableAttribute<A>, AC : TableContext<AC>> :
    AttributeOperation<CTX, A, AC>

/**
 * Row attribute operation associated with row rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class RowAttributeRenderOperation<CTX : RenderingContext, A : RowAttribute<A>, AC : RowContext<AC>> :
    AttributeOperation<CTX, A, AC>


/**
 * Cell attribute operation associated with cell rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class CellAttributeRenderOperation<CTX : RenderingContext, A : CellAttribute<A>> :
    AttributeOperation<CTX, A, CellContext>

/**
 * Column attribute operation associated with column rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class ColumnAttributeRenderOperation<CTX : RenderingContext, A : ColumnAttribute<A>, AC : ColumnContext<AC>> :
    AttributeOperation<CTX, A, AC>

