package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.*
import pl.voytech.exporter.core.template.context.*

interface AttributeOperation<out T : Attribute> {
    fun attributeType(): Class<out T>
    fun priority(): Int = HIGHER

    companion object {
        const val LOWER = -1
        const val HIGHER = 1
    }
}

interface TableAttributeOperation<T : TableAttribute, A> : AttributeOperation<T> {
    fun renderAttribute(state: A, table: Table<*>, attribute: T)
}

interface RowAttributeOperation<E,T : RowAttribute, A> : AttributeOperation<T> {
    fun renderAttribute(state: A, context: AttributedRow<E>, attribute: T)
}

interface CellAttributeOperation<E, T : CellAttribute, A> : AttributeOperation<T> {
    fun renderAttribute(state: A, context: AttributedCell, attribute: T)
}

interface ColumnAttributeOperation<E, T : ColumnAttribute, A> : AttributeOperation<T> {
    fun renderAttribute(state: A, context: AttributedColumn, attribute: T)
}
