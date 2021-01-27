package pl.voytech.exporter.core.template.operations.chain

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.TableOperations

class EmptyOperationChainException : RuntimeException("There is no export operation in the chain.")

class TableOperationChain<T, A>(
    private vararg val chain: TableOperations<T, A>
) : TableOperations<T, A> {

    override fun createTable(state: A, table: Table<T>): Table<T> {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.createTable(state, table) }
        return table
    }

    override fun renderRow(
        state: A,
        context: AttributedRow<T>
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRow(state, context) }
    }

    override fun renderColumn(
        state: A,
        context: AttributedColumn
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach {
            it.renderColumn(
                state,
                context
            )
        }
    }

    override fun renderRowCell(
        state: A,
        context: AttributedCell
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRowCell(state, context) }
    }

}
