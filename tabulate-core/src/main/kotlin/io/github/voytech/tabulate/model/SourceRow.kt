package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.template.context.RowIndex

/**
 * Table row rendering context with row and object coordinates (row number within target table, as well as object
 * index within collection) and row-associated collection entry.
 * Instantiated indirectly by ExportingState class instance.
 * @author Wojciech Mąka
 */
data class SourceRow<T> (
    /**
     * index of a row in entire table (including synthetic rows).
     */
    val rowIndex: RowIndex,
    /**
     * Index of an object within dataset.
     */
    val objectIndex: Int? = null,
    /**
     * Object from collection at objectIndex.
     */
    val record: T? = null
) {
    fun hasRecord(): Boolean = record != null && objectIndex != null

    fun rowIndexValue(): Int = rowIndex.rowIndex
}
