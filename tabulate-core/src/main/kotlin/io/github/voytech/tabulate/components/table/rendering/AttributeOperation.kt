package io.github.voytech.tabulate.components.table.rendering

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
abstract class RowAttributeRenderOperation<CTX : RenderingContext, A : Attribute<A>, AC : RowRenderableEntity> :
    AttributeOperation<CTX, A, AC>


/**
 * Cell attribute operation associated with cell rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class CellAttributeRenderOperation<CTX : RenderingContext, A : Attribute<A>> :
    AttributeOperation<CTX, A, CellRenderableEntity>

/**
 * Column attribute operation associated with column rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class ColumnAttributeRenderOperation<CTX : RenderingContext, A : Attribute<A>, AC : ColumnRenderableEntity> :
    AttributeOperation<CTX, A, AC>

