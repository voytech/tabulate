package io.github.voytech.tabulate.core.template.operations.impl

import io.github.voytech.tabulate.core.template.context.AttributedCell
import io.github.voytech.tabulate.core.template.context.AttributedColumn
import io.github.voytech.tabulate.core.template.context.AttributedRow
import io.github.voytech.tabulate.core.template.operations.TableRenderOperations

class EmptyOperationChainException : RuntimeException("There is no export operation in the chain.")

class TableRenderOperationsChain<T>(
    private vararg val chain: TableRenderOperations<T>
) : TableRenderOperations<T> {

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
