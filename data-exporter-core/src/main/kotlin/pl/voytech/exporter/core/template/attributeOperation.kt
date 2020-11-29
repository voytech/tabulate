package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.*



interface AttributeOperation<out T : Attribute> {
    fun attributeType(): Class<out T>
    fun priority(): Int = HIGHER

    companion object {
        const val LOWER = -1
        const val HIGHER = 1
    }
}

interface TableAttributeOperation<T : TableAttribute, A> : AttributeOperation<T> {
    fun renderAttribute(state: DelegateAPI<A>, table: Table<*>, attribute: T)
}

interface RowAttributeOperation<E,T : RowAttribute, A> : AttributeOperation<T> {
    fun renderAttribute(state: DelegateAPI<A>, context: OperationContext<E,RowOperationTableData<E>>, attribute: T)
}

interface CellAttributeOperation<E, T : CellAttribute, A> : AttributeOperation<T> {
    fun renderAttribute(state: DelegateAPI<A>, context: OperationContext<E,CellOperationTableData<E>>, attribute: T)
}

interface ColumnAttributeOperation<E, T : ColumnAttribute, A> : AttributeOperation<T> {
    fun renderAttribute(state: DelegateAPI<A>, context: OperationContext<E,ColumnOperationTableData<E>>, attribute: T)
}
