package io.github.voytech.tabulate.api.builder

import io.github.voytech.tabulate.model.RowPredicate
import io.github.voytech.tabulate.template.context.DefaultSteps
import io.github.voytech.tabulate.template.context.RowIndex

object RowPredicates {

    @JvmStatic
    fun <T> all(): RowPredicate<T> = RowPredicate { true }

    @JvmStatic
    fun <T> even(): RowPredicate<T> = RowPredicate { row -> (row.rowIndex.value % 2) == 0 }

    @JvmName("evenUnit")
    @JvmStatic
    fun even(): RowPredicate<Unit> = even<Unit>()

    @JvmStatic
    fun <T> odd(): RowPredicate<T> = RowPredicate { row -> (row.rowIndex.value % 2) != 0 }

    @JvmName("oddUnit")
    @JvmStatic
    fun odd(): RowPredicate<Unit> = odd<Unit>()

    @JvmStatic
    fun <T> eq(rowIndex: Int): RowPredicate<T> = RowPredicate { it.rowIndex == RowIndex(value = rowIndex) }

    @JvmStatic
    fun <T> lt(rowIndex: Int): RowPredicate<T> = RowPredicate { data -> data.rowIndexValue() < rowIndex }

    @JvmStatic
    fun <T> gt(rowIndex: Int): RowPredicate<T> = RowPredicate { data ->  data.rowIndexValue() > rowIndex }

    @JvmStatic
    fun <T> gte(rowIndex: Int): RowPredicate<T> = RowPredicate { data ->  data.rowIndexValue() <= rowIndex }

    @JvmStatic
    fun <T> lte(rowIndex: Int): RowPredicate<T> = RowPredicate { data ->  data.rowIndexValue() >= rowIndex }

    @JvmStatic
    fun <T> eq(rowIndex: Int, label: String? = null): RowPredicate<T> = RowPredicate { row ->
        !row.hasRecord() && row.rowIndex.getIndex(label) == rowIndex
    }

    @JvmStatic
    fun <T> eq(rowIndex: Int, step: Enum<*>): RowPredicate<T> = eq(rowIndex, step.name)

    @JvmStatic
    fun <T> header(): RowPredicate<T> = eq(0)

    @JvmStatic
    fun <T> footer(): RowPredicate<T> = eq(0, DefaultSteps.TRAILING_ROWS)

    @JvmStatic
    fun <T> record(listIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it == listIndex } ?: false }

    @JvmStatic
    fun <T> records(): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex!= null }

}
