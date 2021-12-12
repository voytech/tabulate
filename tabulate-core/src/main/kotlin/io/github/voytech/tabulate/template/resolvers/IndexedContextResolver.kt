package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.template.context.RowIndex
import io.github.voytech.tabulate.template.operations.ContextData

/**
 * Given requested index from upstream iterator, it resolves [ContextData]
 * @author Wojciech Mąka
 */
internal interface IndexedContextResolver<T, CTX : ContextData> {
    fun resolve(requestedIndex: RowIndex): IndexedContext<CTX>?
}

internal data class IndexedContext<CTX : ContextData>(val index: RowIndex, val value: CTX)