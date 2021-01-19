package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.template.OperationContext

interface IndexDataResolver<E, T : OperationContext<E>> {
    fun resolve(requestedIndex: Int): T
}
