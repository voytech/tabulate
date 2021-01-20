package pl.voytech.exporter.core.template.iterators

import pl.voytech.exporter.core.template.OperationContext
import pl.voytech.exporter.core.template.resolvers.IndexedContextResolver

class BaseTableDataIterator<CTX>(
    private val resolver: IndexedContextResolver<CTX>
) : AbstractIterator<OperationContext<CTX>>() {
    private var currentIndex: Int = -1
    private var currentContext: OperationContext<CTX>? = null

    override fun computeNext() {
        resolver.resolve(currentIndex++).also {
            if (it != null) {
                currentContext = it.value
                currentIndex = it.index
                setNext(currentContext!!)
            } else {
                done()
            }
        }
    }
}
