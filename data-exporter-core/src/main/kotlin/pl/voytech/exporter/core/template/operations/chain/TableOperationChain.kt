package pl.voytech.exporter.core.template.operations.chain

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
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
        extensions: Set<RowExtension>?
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRow(state, context, extensions) }
    }

    override fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        extensions: Set<ColumnExtension>?
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach {
            it.renderColumn(
                state,
                context,
                extensions
            )
        }
    }

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        extensions: Set<CellExtension>?
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRowCell(state, context, extensions) }
    }

}