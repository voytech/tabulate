package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.core.model.ContinuationsApi

data class TableContinuations(val continuations: ContinuationsApi) {

    internal fun <T> newContinuation(def: ColumnDef<T>) {
        continuations.newContinuation(NEXT_COLUMN to def.index)
    }

    internal fun newContinuation(rowIndex: RowIndex, recordIndex: Int) {
        continuations.newContinuation(NEXT_ROW to rowIndex, NEXT_RECORD to recordIndex)
    }

    private fun getContinuationColumnIndex(): Int? = continuations.getCurrentContinuationAttributeOrNull(NEXT_COLUMN)

    internal fun getContinuationColumnIndexOrZero(): Int = getContinuationColumnIndex() ?: 0

    internal fun getContinuationRowIndex(): RowIndex? = continuations.getCurrentContinuationAttributeOrNull(NEXT_ROW)

    internal fun getContinuationRowIndexOrZero(): Int = getContinuationRowIndex()?.value ?: 0

    internal fun getContinuationRecordIndex(): Int? = continuations.getCurrentContinuationAttributeOrNull(NEXT_RECORD)

    fun isValid(column: Int): Boolean =
        getContinuationColumnIndex()?.let {
            column >= it
        } ?: true

    fun <T> Iterable<T>?.crop(): Iterable<T>? =
        getContinuationRecordIndex()?.let {
            this?.drop(it)?.asIterable()
        } ?: this

    internal fun <T> List<ColumnDef<T>>.crop(): List<ColumnDef<T>> =
        getContinuationColumnIndex()?.let {
            drop(it)
        } ?: this

    companion object {
        const val NEXT_COLUMN = "next_column"
        const val NEXT_ROW = "next_row"
        const val NEXT_RECORD = "next_record"
    }
}

