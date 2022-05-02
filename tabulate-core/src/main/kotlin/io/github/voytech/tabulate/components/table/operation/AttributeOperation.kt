package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.TableAttribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AbstractAttributeOperation

/**
 * Table attribute operation associated with table rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class TableAttributeRenderOperation<CTX : RenderingContext, ATTR : TableAttribute<*>, AC : TableContext>(private val context: Class<AC>) :
    AbstractAttributeOperation<CTX, Table<Any>, TableAttribute<*>, ATTR, AC>() {
    override fun operationContextClass(): Class<AC> = context
    override fun classifier(): AttributeClassifier<TableAttribute<*>, Table<Any>> = AttributeClassifier.classify()
}

/**
 * Row attribute operation associated with row rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class RowAttributeRenderOperation<CTX : RenderingContext, ATTR : RowAttribute<*>, AC : RowContext>(private val context: Class<AC>) :
    AbstractAttributeOperation<CTX, Table<Any>, RowAttribute<*>, ATTR, AC>() {
    override fun operationContextClass(): Class<AC> = context
    override fun classifier(): AttributeClassifier<RowAttribute<*>, Table<Any>> = AttributeClassifier.classify()
}


/**
 * Cell attribute operation associated with cell rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class CellAttributeRenderOperation<CTX : RenderingContext, ATTR : CellAttribute<*>> :
    AbstractAttributeOperation<CTX, Table<Any>, CellAttribute<*>, ATTR, CellContext>() {
    override fun operationContextClass(): Class<CellContext> = reify()
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<Any>> = AttributeClassifier.classify()
}

/**
 * Column attribute operation associated with column rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class ColumnAttributeRenderOperation<CTX : RenderingContext, ATTR : ColumnAttribute<*>, AC : ColumnContext>(private val context: Class<AC>) :
    AbstractAttributeOperation<CTX, Table<Any>, ColumnAttribute<*>, ATTR, AC>() {
    override fun operationContextClass(): Class<AC> = context

    override fun classifier(): AttributeClassifier<ColumnAttribute<*>, Table<Any>> = AttributeClassifier.classify()
}

