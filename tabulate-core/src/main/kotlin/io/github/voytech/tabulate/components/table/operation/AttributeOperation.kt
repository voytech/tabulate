package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.operation.AttributeOperation

/**
 * Table attribute operation associated with table rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class TableAttributeRenderOperation<CTX : RenderingContext, A : Attribute<A>, AC : TableContext> :
    AttributeOperation<CTX, A, AC>

/**
 * Row attribute operation associated with row rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class RowAttributeRenderOperation<CTX : RenderingContext, A : Attribute<A>, AC : RowRenderable> :
    AttributeOperation<CTX, A, AC>


/**
 * Cell attribute operation associated with cell rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class CellAttributeRenderOperation<CTX : RenderingContext, A : Attribute<A>> :
    AttributeOperation<CTX, A, CellRenderable>

/**
 * Column attribute operation associated with column rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class ColumnAttributeRenderOperation<CTX : RenderingContext, A : Attribute<A>, AC : ColumnRenderable> :
    AttributeOperation<CTX, A, AC>

