package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute
import pl.voytech.exporter.core.template.*

abstract class AttributesHandlingTableOperations<T, A>(
    tableAttributeOperations: List<TableAttributeOperation<out TableAttribute, A>>?,
    rowAttributeOperations: List<RowAttributeOperation<T, out RowAttribute, A>>?,
    columnAttributeOperations: List<ColumnAttributeOperation<T, out ColumnAttribute, A>>?,
    cellAttributeOperations: List<CellAttributeOperation<T, out CellAttribute, A>>?
) : TableOperations<T, A> {

    private val delegates: DelegatingAttributesOperations<T, A> =
        DelegatingAttributesOperations(
            tableAttributeOperations, columnAttributeOperations, rowAttributeOperations, cellAttributeOperations
        )

    override fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A> {
        return initializeTable(state, table).also {
            table.tableAttributes?.let { delegates.applyTableAttributes(state, table, it) }
        }
    }

    abstract fun initializeTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>

    override fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        attributes: Set<RowAttribute>?
    ) {
        renderRow(state, context).also {
            attributes?.let { delegates.applyRowAttributes(state, context, it) }
        }
    }

    abstract fun renderRow(state: DelegateAPI<A>, context: OperationContext<T, RowOperationTableData<T>>)

    override fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        attributes: Set<ColumnAttribute>?
    ) {
        attributes?.let { delegates.applyColumnAttributes(state, context, attributes) }
    }

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        attributes: Set<CellAttribute>?
    ) {
        renderRowCell(state, context).also {
            attributes?.let { delegates.applyCellAttributes(state, context, it) }
        }
    }

    abstract fun renderRowCell(state: DelegateAPI<A>, context: OperationContext<T, CellOperationTableData<T>>)

}

