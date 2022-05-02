package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.operation.RowEnd

/**
 * An iterator providing instance of [RowEnd] applicable for requested row index.
 * Internally it delegates [RowEnd] resolving logic to managed [IndexedContextResolver]
 * @since 0.1.0
 * @author Wojciech Mąka
 */
internal class RowContextIterator<T>(
    private val resolver: IndexedContextResolver<T, RowEnd<T>>
) : AbstractIterator<RowEnd<T>>() {

    private val indexIncrement = MutableRowIndex()

    override fun computeNext() {
        resolver.resolve(indexIncrement.getRowIndex()).also {
            if (it != null) {
                val currentContext = it.value
                indexIncrement.set(it.index)
                setNext(currentContext)
                indexIncrement.inc()
            } else done()
        }
    }

}

