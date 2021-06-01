package io.github.voytech.tabulate.template.iterators

import io.github.voytech.tabulate.template.context.ContextData
import io.github.voytech.tabulate.template.context.ExportingStateReceiver
import io.github.voytech.tabulate.template.context.TableExportingState
import io.github.voytech.tabulate.template.resolvers.IndexedContextResolver

class OperationContextIterator<T, CTX : ContextData<T>>(
    private val resolver: IndexedContextResolver<T, CTX>
) : AbstractIterator<CTX>(), ExportingStateReceiver<T> {
    private lateinit var exportingState: TableExportingState<T>

    override fun computeNext() {
        resolver.resolve(exportingState.getAndIncrement()).also {
            if (it != null) {
                val currentContext = it.value
                if (it.index > exportingState.getRowIndex()) {
                    exportingState.setRowIndex(it.index)
                }
                setNext(currentContext)
            } else {
                done()
            }
        }
    }

    override fun setState(exportingState: TableExportingState<T>) {
        this.exportingState = exportingState
        resolver.setState(exportingState)
    }
}
