package io.github.voytech.tabulate.api.builder

import io.github.voytech.tabulate.model.RowPredicate
import io.github.voytech.tabulate.template.context.DefaultSteps
import io.github.voytech.tabulate.template.context.RowIndex

object RowPredicates {
    @JvmStatic
    fun <T> allRows(): RowPredicate<T> = RowPredicate{ true }

    @JvmStatic
    fun <T> even(): RowPredicate<T> = RowPredicate { row ->
        (row.rowIndex.value % 2) == 0
    }

    @JvmName("evenUnit")
    @JvmStatic
    fun even(): RowPredicate<Unit> = RowPredicate { row ->
        (row.rowIndex.value % 2) == 0
    }

    @JvmStatic
    fun <T> odd(): RowPredicate<T> = RowPredicate { row ->
        (row.rowIndex.value % 2) != 0
    }

    @JvmStatic
    fun <T> isCustomAt(rowIndex: Int, label: String ? = null): RowPredicate<T> = RowPredicate { row ->
        !row.hasRecord() && row.rowIndex.getIndex(label) == rowIndex
    }

    @JvmStatic
    fun <T> isCustomAt(rowIndex: Int, label: DefaultSteps): RowPredicate<T> = isCustomAt(rowIndex, label.name)

    @JvmStatic
    fun <T> isHeaderPosition(): RowPredicate<T> = isCustomAt(0)

    @JvmStatic
    fun <T> isFooterPosition(): RowPredicate<T> = isCustomAt(0, DefaultSteps.TRAILING_ROWS)

    @JvmStatic
    fun <T> hasEntryAt(listIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it == listIndex } ?: false }

    @JvmStatic
    fun <T> isAt(rowIndex: Int): RowPredicate<T> = RowPredicate {
        it.rowIndex == RowIndex(value = rowIndex)
    }

    @JvmStatic
    fun <T> lt(rowIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it < rowIndex } ?: false }

    @JvmStatic
    fun <T> gt(rowIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it > rowIndex } ?: false }
}
