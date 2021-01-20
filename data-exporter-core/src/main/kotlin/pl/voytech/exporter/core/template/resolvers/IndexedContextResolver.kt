package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.template.OperationContext

interface IndexedContextResolver<CTX> {
    fun resolve(requestedIndex: Int): IndexedValue<OperationContext<CTX>>?
}
