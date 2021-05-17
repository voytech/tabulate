package io.github.voytech.tabulate.model



fun interface RowCellExpression<T> {
    fun evaluate(context: SourceRow<T>): Any?
}

object RowSelectors {
    @JvmStatic
    fun <T> allRows(): RowPredicate<T> = RowPredicate{ true }

    @JvmStatic
    fun <T> createAt(rowIndex: Int): RowPredicate<T> = RowPredicate { it.rowIndex == rowIndex && !it.hasRecord()}

    @JvmStatic
    fun <T> asTableHeader(): RowPredicate<T> = createAt(0)

    @JvmStatic
    fun <T> atListIndex(listIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it == listIndex } ?: false }

    @JvmStatic
    fun <T> atRowIndex(rowIndex: Int): RowPredicate<T> = RowPredicate { data -> data.rowIndex == rowIndex }

    @JvmStatic
    fun <T> lowerThan(rowIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it < rowIndex } ?: false }

    @JvmStatic
    fun <T> higherTan(rowIndex: Int): RowPredicate<T> =
        RowPredicate { data -> data.objectIndex?.let { it > rowIndex } ?: false }
}
