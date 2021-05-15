package io.github.voytech.tabulate.core.template.operations

import io.github.voytech.tabulate.core.model.Table
import io.github.voytech.tabulate.core.model.attributes.Attribute
import io.github.voytech.tabulate.core.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.core.template.context.ColumnContext
import io.github.voytech.tabulate.core.template.context.RowCellContext
import io.github.voytech.tabulate.core.template.context.RowContext

interface AttributeOperation<out T : Attribute<*>> {
    fun attributeType(): Class<out T>
    fun priority(): Int = HIGHER

    companion object {
        const val LOWER = -1
        const val HIGHER = 1
    }
}

interface TableAttributeRenderOperation<T : TableAttribute> : AttributeOperation<T> {
    fun renderAttribute(table: Table<*>, attribute: T)
}

interface RowAttributeRenderOperation<E,T : RowAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: RowContext<E>, attribute: T)
}

interface CellAttributeRenderOperation<T : CellAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: RowCellContext, attribute: T)
}

interface ColumnAttributeRenderOperation<T : ColumnAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: ColumnContext, attribute: T)
}

interface AttributeRenderOperationsFactory<T> {
    fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<out TableAttribute>>? = null
    fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<T, out RowAttribute>>? = null
    fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<out ColumnAttribute>>? = null
    fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttribute>>? = null
}

abstract class AdaptingTableAttributeRenderOperation<A, T: TableAttribute>(open val adaptee: A): TableAttributeRenderOperation<T>

abstract class AdaptingRowAttributeRenderOperation<A, E, T: RowAttribute>(open val adaptee: A): RowAttributeRenderOperation<E, T>

abstract class AdaptingCellAttributeRenderOperation<A, T: CellAttribute>(open val adaptee: A): CellAttributeRenderOperation<T>

abstract class AdaptingColumnAttributeRenderOperation<A, T: ColumnAttribute>(open val adaptee: A): ColumnAttributeRenderOperation<T>

