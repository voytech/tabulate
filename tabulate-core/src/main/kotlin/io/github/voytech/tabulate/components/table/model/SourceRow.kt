package io.github.voytech.tabulate.components.table.model

import io.github.voytech.tabulate.components.table.template.RowIndex

/**
 * Wraps collection element for further row context evaluation.
 * It adds extra information for row context resolving:
 *  - current row index value,
 *  - index of a collection element,
 *  - collection item
 *  Collection item and index information is then used by row predicates in order to match all table row definitions.
 *
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class SourceRow<T> (
    /**
     * index of a row in table (including both: custom and collection sourced rows).
     */
    val rowIndex: RowIndex,
    /**
     * Index of an element within collection.
     */
    val objectIndex: Int? = null,
    /**
     * Object from collection at objectIndex.
     */
    val record: T? = null
) {
    fun hasRecord(): Boolean = record != null && objectIndex != null

    fun rowIndexValue(): Int = rowIndex.value
}
