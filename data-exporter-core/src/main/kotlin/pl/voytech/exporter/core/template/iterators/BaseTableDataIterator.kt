package pl.voytech.exporter.core.template.iterators

import pl.voytech.exporter.core.template.OperationContext
import pl.voytech.exporter.core.template.resolvers.IndexedContextResolver
import java.util.concurrent.atomic.AtomicInteger

class BaseTableDataIterator<CTX>(
    private val resolver: IndexedContextResolver<CTX>
) : AbstractIterator<OperationContext<CTX>>() {
    private val currentIndex = AtomicInteger(0)
    private var currentContext: OperationContext<CTX>? = null

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

    fun getCurrentIndex(): Int = currentIndex.get()

    fun getCurrentContext(): OperationContext<CTX>? = currentContext
}
