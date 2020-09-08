package pl.voytech.exporter.core.model

typealias RowSelector<T> = (context: TypedRowData<T>) -> Boolean

typealias RowCellEval<T> = (context: TypedRowData<T>) -> Any?

object RowSelectors {
    @JvmStatic
    fun <T> all(): RowSelector<T> = { _ -> true }

    @JvmStatic
    fun <T> createAt(rowIndex: Int): RowSelector<T> =
        { data -> data.rowIndex == rowIndex && data.objectIndex == null && data.record == null }

    @JvmStatic
    fun <T> atListIndex(listIndex: Int): RowSelector<T> = { data -> data.objectIndex!! == listIndex }

    @JvmStatic
    fun <T> atRowIndex(rowIndex: Int): RowSelector<T> = { data -> data.rowIndex == rowIndex }

    @JvmStatic
    fun <T> lowerThan(rowIndex: Int): RowSelector<T> = { data -> data.objectIndex!! < rowIndex }

    @JvmStatic
    fun <T> higherTan(rowIndex: Int): RowSelector<T> = { data -> data.objectIndex!! > rowIndex }
}