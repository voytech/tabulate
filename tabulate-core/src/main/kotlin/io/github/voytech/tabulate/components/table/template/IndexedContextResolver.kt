package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.core.operation.ContextData

/**
 * Given requested index from upstream iterator, it resolves [ContextData]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal interface IndexedContextResolver<CTX : ContextData> {
    fun resolve(requestedIndex: RowIndex): IndexedResult<CTX>?
}

internal data class IndexedResult<CTX : ContextData>(
    val rowIndex: RowIndex,
    val sourceRecordIndex: Int? = null,
    val result: ContextResult<CTX>
)