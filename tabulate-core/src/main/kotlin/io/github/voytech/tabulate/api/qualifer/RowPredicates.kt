package io.github.voytech.tabulate.api.qualifer

import io.github.voytech.tabulate.model.RowPredicate
import io.github.voytech.tabulate.template.context.IndexLabel
import io.github.voytech.tabulate.template.context.RowIndex

object RowPredicates {
    @JvmStatic
    fun <T> allRows(): RowPredicate<T> = RowPredicate{ true }

    @JvmStatic
    fun <T> isCustomAt(rowIndex: Int, label: String ? = null): RowPredicate<T> = RowPredicate { row ->
        !row.hasRecord() && row.rowIndex.getIndex(label) == rowIndex
    }

    @JvmStatic
    fun <T> isCustomAt(rowIndex: Int, label: IndexLabel): RowPredicate<T> = isCustomAt(rowIndex, label.name)

    @JvmStatic
    fun <T> isHeaderPosition(): RowPredicate<T> = isCustomAt(0)

    @JvmStatic
    fun <T> isFooterPosition(): RowPredicate<T> = isCustomAt(0, IndexLabel.TRAILING_ROWS)

    @JvmStatic
    fun <T> hasEntryAt(listIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it == listIndex } ?: false }

    @JvmStatic
    fun <T> isAt(rowIndex: Int): RowPredicate<T> = RowPredicate {
        it.rowIndex == RowIndex(rowIndex = rowIndex)
    }

    @JvmStatic
    fun <T> isLowerThan(rowIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it < rowIndex } ?: false }

    @JvmStatic
    fun <T> isHigherThan(rowIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it > rowIndex } ?: false }
}
