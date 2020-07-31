package pl.voytech.exporter.core.template.operations.chain

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.template.*

class EmptyOperationChainException : RuntimeException("There is no export operation in the chain.")

class RowOperations<T, A>(
    private vararg val chain: RowOperation<T, A>
) : RowOperation<T, A> {

    override fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        extensions: Set<RowExtension>?
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRow(state, context, extensions) }
    }
}

class ColumnOperations<T, A>(
    private vararg val chain: ColumnOperation<T, A>
) : ColumnOperation<T, A> {

    override fun beforeFirstRow(): Boolean = true

    override fun afterLastRow(): Boolean = true

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
}

class RowCellOperations<T, A>(
    private vararg val chain: RowCellOperation<T, A>
) : RowCellOperation<T, A> {

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        extensions: Set<CellExtension>?
    ) {
        chain.ifEmpty { throw EmptyOperationChainException() }
        chain.forEach { it.renderRowCell(state, context, extensions) }
    }
}