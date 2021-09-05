package io.github.voytech.tabulate.template.operations.impl

import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.AttributedColumn
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.operations.TableExportOperations

class EmptyOperationChainException : RuntimeException("There is no export operation in the chain.")

class TableExportOperationsChain<T>(
    private vararg val chain: TableExportOperations<T>
) : TableExportOperations<T> {

    override fun beginRow(
        context: AttributedRow<T>
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.beginRow(context) }
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
