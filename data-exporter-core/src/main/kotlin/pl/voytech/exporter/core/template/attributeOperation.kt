package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.*
import kotlin.reflect.KClass


interface AttributeOperation<out T : Attribute> {
    fun attributeType(): KClass<out T>
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
