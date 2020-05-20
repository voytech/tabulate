package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.hints.*
import kotlin.reflect.KClass


interface HintOperation<out T : Hint> {
    fun hintType(): KClass<out T>
}

interface TableHintOperation<T: TableHint> : HintOperation<T> {
    fun apply(state: DelegateState, hint: T)
}

interface RowHintOperation<T: RowHint> : HintOperation<T> {
    fun apply(state: DelegateState, rowIndex: Int, hint: T)
}

interface CellHintOperation<T: CellHint> : HintOperation<T> {
    fun apply(state: DelegateState, coordinates: Coordinates, hint: T)
}

interface ColumnHintOperation<T: ColumnHint> : HintOperation<T> {
    fun apply(state: DelegateState, columnIndex: Int, hint: T)
}
