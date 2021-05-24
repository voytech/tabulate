package io.github.voytech.tabulate.template.operations.impl

import io.github.voytech.tabulate.template.ResultHandler
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.AttributedColumn
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.operations.TableExportOperations
import org.reactivestreams.Publisher

class EmptyOperationChainException : RuntimeException("There is no export operation in the chain.")

class TableExportOperationsChain<T,O>(
    private vararg val chain: TableExportOperations<T,O>
) : TableExportOperations<T,O> {

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

    override fun initialize(source: Publisher<T>, resultHandler: ResultHandler<T, O>) {
        TODO("Not yet implemented")
    }

    override fun finish() {
        TODO("Not yet implemented")
    }

}
