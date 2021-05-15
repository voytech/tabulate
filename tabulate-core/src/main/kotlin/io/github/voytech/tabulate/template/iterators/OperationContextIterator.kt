package io.github.voytech.tabulate.template.iterators

import io.github.voytech.tabulate.template.context.ContextData
import io.github.voytech.tabulate.template.resolvers.IndexedContextResolver
import java.util.concurrent.atomic.AtomicInteger

class OperationContextIterator<T, CTX : ContextData<T>>(
    private val resolver: IndexedContextResolver<T, CTX>
) : AbstractIterator<CTX>() {
    private val currentIndex = AtomicInteger(0)
    private var currentContext: CTX? = null

    override fun computeNext() {
        resolver.resolve(currentIndex.getAndIncrement()).also {
            if (it != null) {
                currentContext = it.value
                if (it.index > currentIndex.get()) {
                    currentIndex.set(it.index)
                }
                setNext(currentContext!!)
            } else {
                done()
            }
        }
    }
}
