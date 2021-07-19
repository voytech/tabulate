package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.template.context.ContextData
import io.github.voytech.tabulate.template.context.RowIndex


interface IndexedContextResolver<T, CTX : ContextData<T>> {
    fun resolve(requestedIndex: RowIndex): IndexedValue<CTX>?
}
