package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.template.context.ContextData

interface IndexedContextResolver<T, CTX : ContextData<T>> {
    fun resolve(requestedIndex: Int): IndexedValue<CTX>?
}
