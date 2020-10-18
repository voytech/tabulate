package pl.voytech.exporter.core.template.operations.chain

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.template.*

class EmptyOperationChainException : RuntimeException("There is no export operation in the chain.")

class TableOperationChain<T, A>(
    private vararg val chain: TableOperations<T, A>
) : TableOperations<T, A> {

    override fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A> {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.createTable(state, table) }
        return state
    }

    override fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        attributes: Set<RowAttribute>?
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRow(state, context, attributes) }
    }

    override fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        attributes: Set<ColumnAttribute>?
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach {
            it.renderColumn(
                state,
                context,
                attributes
            )
        }
    }

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        attributes: Set<CellAttribute>?
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRowCell(state, context, attributes) }
    }

}