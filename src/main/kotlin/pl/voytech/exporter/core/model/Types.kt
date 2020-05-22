package pl.voytech.exporter.core.model

typealias RowSelector<T> = (context: RowData<T>) -> Boolean

typealias RowCellEval<T> = (context: RowData<T>) -> Any?

object RowSelectors {
    fun <T> all(): RowSelector<T> = {_ -> true}
    fun <T> at(rowIndex: Int): RowSelector<T> = {data -> data.index == rowIndex}
    fun <T> lowerThan(rowIndex: Int): RowSelector<T> = {data -> data.index < rowIndex}
    fun <T> higherTan(rowIndex: Int): RowSelector<T> = {data -> data.index > rowIndex}
}