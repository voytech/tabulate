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

interface TableAttributeOperation<T : TableAttribute> : AttributeOperation<T> {
    fun renderAttribute(table: Table<*>, attribute: T)
}

interface RowAttributeOperation<E,T : RowAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: AttributedRow<E>, attribute: T)
}

interface CellAttributeOperation<E, T : CellAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: AttributedCell, attribute: T)
}

interface ColumnAttributeOperation<E, T : ColumnAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: AttributedColumn, attribute: T)
}

abstract class AdaptingTableAttributeOperation<A, T: TableAttribute>(open val adaptee: A): TableAttributeOperation<T>

abstract class AdaptingRowAttributeOperation<A, E, T: RowAttribute>(open val adaptee: A): RowAttributeOperation<E, T>

abstract class AdaptingCellAttributeOperation<A, E,  T: CellAttribute>(open val adaptee: A): CellAttributeOperation<E, T>

abstract class AdaptingColumnAttributeOperation<A, E, T: ColumnAttribute>(open val adaptee: A): ColumnAttributeOperation<E, T>
