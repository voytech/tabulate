package io.github.voytech.tabulate.template.iterators

import io.github.voytech.tabulate.template.context.ContextData
import io.github.voytech.tabulate.template.context.MutableRowIndex
import io.github.voytech.tabulate.template.resolvers.IndexedContextResolver

class OperationContextIterator<T, CTX : ContextData<T>>(
    private val resolver: IndexedContextResolver<T, CTX>,
    private val indexIncrement: MutableRowIndex
) : AbstractIterator<CTX>() {

    override fun computeNext() {
        resolver.resolve(indexIncrement.getRowIndex()).also {
            if (it != null) {
                val currentContext = it.value
                if (it.index > indexIncrement.getRowIndex().rowIndex) {
                    indexIncrement.assign(it.index)
                }
                setNext(currentContext)
                indexIncrement.inc()
            } else {
                done()
            }
        }
    }
}

