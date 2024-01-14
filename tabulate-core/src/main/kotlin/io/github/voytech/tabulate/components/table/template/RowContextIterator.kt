package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.rendering.RowEndRenderable

/**
 * An iterator providing instance of [RowEndRenderable] applicable for requested row index.
 * Internally it delegates [RowEndRenderable] resolving logic to managed [IndexedContextResolver]
 * @since 0.1.0
 * @author Wojciech Mąka
 */
internal class RowContextIterator<T: Any>(
    private val resolver: IndexedContextResolver<RowEndRenderable<T>>,
    private val tableIterations: TableRenderIterations
) : AbstractIterator<ContextResult<RowEndRenderable<T>>>() {

    private var nextSourceRecordIndex: Int = tableIterations.getStartRecordIndex() ?: 0

    private val indexIncrement = MutableRowIndex().apply {
        tableIterations.getStartRowIndex()?.let { set(it) }
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
                        tableIterations.pushNewIteration(it.rowIndex, nextSourceRecordIndex)
                        setNext(it.result)
                    }
                }
            } else done()
        }
    }

}

