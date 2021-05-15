package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.template.context.ContextData

interface IndexedContextResolver<T, CTX : ContextData<T>> {
    fun resolve(requestedIndex: Int): IndexedValue<CTX>?
}
