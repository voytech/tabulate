package io.github.voytech.tabulate.template.iterators

import io.github.voytech.tabulate.template.context.MutableRowIndex
import io.github.voytech.tabulate.template.operations.AttributedRowWithCells
import io.github.voytech.tabulate.template.resolvers.IndexedContextResolver

internal class RowContextIterator<T>(
    private val resolver: IndexedContextResolver<T, AttributedRowWithCells<T>>
) : AbstractIterator<AttributedRowWithCells<T>>() {

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

