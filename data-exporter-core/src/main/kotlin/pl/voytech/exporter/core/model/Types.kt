package pl.voytech.exporter.core.model


typealias RowSelector<T> = (context: RowData<T>) -> Boolean

typealias RowCellEval<T> = (context: RowData<T>) -> Any?

object RowSelectors {
    fun <T> all(): RowSelector<T> = { _ -> true }
    fun <T> createAt(rowIndex: Int): RowSelector<T> = { data -> data.rowIndex == rowIndex && data.objectIndex == null && data.record == null}
    fun <T> atListIndex(listIndex: Int): RowSelector<T> = { data -> data.objectIndex!! == listIndex }
    fun <T> atRowIndex(rowIndex: Int): RowSelector<T> = { data -> data.rowIndex == rowIndex }
    fun <T> lowerThan(rowIndex: Int): RowSelector<T> = { data -> data.objectIndex!! < rowIndex }
    fun <T> higherTan(rowIndex: Int): RowSelector<T> = { data -> data.objectIndex!! > rowIndex }
}