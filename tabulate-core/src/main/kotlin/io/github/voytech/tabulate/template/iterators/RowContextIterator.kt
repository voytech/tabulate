package io.github.voytech.tabulate.template.iterators

import io.github.voytech.tabulate.template.context.MutableRowIndex
import io.github.voytech.tabulate.template.operations.RowClosingContext
import io.github.voytech.tabulate.template.resolvers.IndexedContextResolver

/**
 * An iterator providing instance of [RowClosingContext] applicable for requested row index.
 * Internally it delegates [RowClosingContext] resolving logic to managed [IndexedContextResolver]
 * @since 0.1.0
 * @author Wojciech Mąka
 */
internal class RowContextIterator<T>(
    private val resolver: IndexedContextResolver<T, RowClosingContext<T>>
) : AbstractIterator<RowClosingContext<T>>() {

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

