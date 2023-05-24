package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.operation.RowEndRenderable

/**
 * An iterator providing instance of [RowEndRenderable] applicable for requested row index.
 * Internally it delegates [RowEndRenderable] resolving logic to managed [IndexedContextResolver]
 * @since 0.1.0
 * @author Wojciech Mąka
 */
internal class RowContextIterator<T: Any>(
    private val resolver: IndexedContextResolver<RowEndRenderable<T>>,
    private val tableContinuations: TableContinuations
) : AbstractIterator<ContextResult<RowEndRenderable<T>>>() {

    private var nextSourceRecordIndex: Int = tableContinuations.getContinuationRecordIndex() ?: 0

    private val indexIncrement = MutableRowIndex().apply {
        tableContinuations.getContinuationRowIndex()?.let { set(it) }
    }

    private fun IndexedResult<RowEndRenderable<T>>.setProcessedSourceRecordIndex() {
        if (sourceRecordIndex != null) {
            nextSourceRecordIndex ++
        }
    }

    override fun computeNext() {
        resolver.resolve(indexIncrement.getRowIndex()).also {
            if (it != null) {
                when (it.result) {
                    is SuccessResult -> {
                        it.setProcessedSourceRecordIndex()
                        indexIncrement.set(it.rowIndex)
                        setNext(it.result)
                        indexIncrement.inc()
                    }
                    is OverflowResult -> {
                        tableContinuations.newContinuation(it.rowIndex, nextSourceRecordIndex)
                        setNext(it.result)
                    }
                }
            } else done()
        }
    }

}

