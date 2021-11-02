package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.template.context.RowIndex

/**
 * This class wraps dataset record for further row context evaluation.
 * It adds additional information for context resolving:
 *  - current row index increment value
 *  - index of an dataset entry
 *  Raw dataset record and index information is then used by row predicates in order to figure out what row definitions
 *  should be applied and when.
 *
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

    fun rowIndexValue(): Int = rowIndex.value
}
