package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.operation.RowEnd
import io.github.voytech.tabulate.core.model.ModelExportContext

/**
 * An iterator providing instance of [RowEnd] applicable for requested row index.
 * Internally it delegates [RowEnd] resolving logic to managed [IndexedContextResolver]
 * @since 0.1.0
 * @author Wojciech Mąka
 */
internal class RowContextIterator<T: Any>(
    private val resolver: IndexedContextResolver<RowEnd<T>>,
    private val overflowOffsets: OverflowOffsets,
    private val templateContext: ModelExportContext
) : AbstractIterator<ContextResult<RowEnd<T>>>() {

    private var nextSourceRecordIndex: Int? = null

    private val indexIncrement = MutableRowIndex().apply {
        set(overflowOffsets.getRowOffset())
    }

    private fun IndexedResult<RowEnd<T>>.setProcessedSourceRecordIndex() {
        if (sourceRecordIndex != null) {
            nextSourceRecordIndex = sourceRecordIndex + 1
        }
    }

    override fun computeNext(): Unit = with(templateContext) {
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
                        suspendY()
                        overflowOffsets.setNextRowIndex(it.rowIndex)
                        overflowOffsets.setNextRecordIndex(nextSourceRecordIndex ?: 0)
                        setNext(it.result)
                    }
                }
            } else done()
        }
    }

}

