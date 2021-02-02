package pl.voytech.exporter.core.template.operations.impl

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.TableRenderOperations

class EmptyOperationChainException : RuntimeException("There is no export operation in the chain.")

class TableRenderOperationsChain<T>(
    private vararg val chain: TableRenderOperations<T>
) : TableRenderOperations<T> {

    override fun createTable(table: Table<T>): Table<T> {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.createTable(table) }
        return table
    }

    override fun renderRow(
        context: AttributedRow<T>
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRow(context) }
    }

    override fun renderColumn(
        context: AttributedColumn
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderColumn(context) }
    }

    override fun renderRowCell(context: AttributedCell) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRowCell(context) }
    }

}
