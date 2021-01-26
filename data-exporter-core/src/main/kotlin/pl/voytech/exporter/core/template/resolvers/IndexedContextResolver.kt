package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.template.context.OperationContext

interface IndexedContextResolver<T, CTX : OperationContext<*>> {
    fun resolve(requestedIndex: Int): IndexedValue<CTX>?
}
