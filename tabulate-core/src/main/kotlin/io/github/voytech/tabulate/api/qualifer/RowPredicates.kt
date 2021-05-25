package io.github.voytech.tabulate.api.qualifer

import io.github.voytech.tabulate.model.RowPredicate

object RowPredicates {
    @JvmStatic
    fun <T> allRows(): RowPredicate<T> = RowPredicate{ true }

    @JvmStatic
    fun <T> isCustomAt(rowIndex: Int): RowPredicate<T> = RowPredicate { it.rowIndex == rowIndex && !it.hasRecord()}

    @JvmStatic
    fun <T> isHeaderPosition(): RowPredicate<T> = isCustomAt(0)

    @JvmStatic
    fun <T> hasEntryAt(listIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it == listIndex } ?: false }

    @JvmStatic
    fun <T> isAt(rowIndex: Int): RowPredicate<T> = RowPredicate { it.rowIndex == rowIndex }

    @JvmStatic
    fun <T> isLowerThan(rowIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it < rowIndex } ?: false }

    @JvmStatic
    fun <T> isHigherThan(rowIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it > rowIndex } ?: false }
}
