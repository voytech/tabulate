package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.core.operation.CustomAttributesData

/**
 * Given requested index from upstream iterator, it resolves [CustomAttributesData]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal interface IndexedContextResolver<CTX : CustomAttributesData> {
    fun resolve(requestedIndex: RowIndex): IndexedResult<CTX>?
}

internal data class IndexedResult<CTX : CustomAttributesData>(
    val rowIndex: RowIndex,
    val sourceRecordIndex: Int? = null,
    val result: ContextResult<CTX>
)