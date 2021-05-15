package io.github.voytech.tabulate.core.template.resolvers

import io.github.voytech.tabulate.core.template.context.ContextData

interface IndexedContextResolver<T, CTX : ContextData<T>> {
    fun resolve(requestedIndex: Int): IndexedValue<CTX>?
}
