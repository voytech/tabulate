package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.Attribute
import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.model.attributes.alias.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.alias.RowAttribute
import pl.voytech.exporter.core.model.attributes.alias.TableAttribute
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow

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
    fun renderAttribute(context: AttributedRow<E>, attribute: T)
}

interface CellAttributeRenderOperation<E, T : CellAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: AttributedCell, attribute: T)
}

interface ColumnAttributeRenderOperation<E, T : ColumnAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: AttributedColumn, attribute: T)
}

interface AttributeRenderOperationsFactory<T> {
    fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<out TableAttribute>>? = null
    fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<T, out RowAttribute>>? = null
    fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<T, out ColumnAttribute>>? = null
    fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<T, out CellAttribute>>? = null
}

abstract class AdaptingTableAttributeRenderOperation<A, T: TableAttribute>(open val adaptee: A): TableAttributeRenderOperation<T>

abstract class AdaptingRowAttributeRenderOperation<A, E, T: RowAttribute>(open val adaptee: A): RowAttributeRenderOperation<E, T>

abstract class AdaptingCellAttributeRenderOperation<A, E,  T: CellAttribute>(open val adaptee: A): CellAttributeRenderOperation<E, T>

abstract class AdaptingColumnAttributeRenderOperation<A, E, T: ColumnAttribute>(open val adaptee: A): ColumnAttributeRenderOperation<E, T>

