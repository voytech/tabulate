package pl.voytech.exporter.core.model

object ColumnNextId {

    private var columnNextId: ThreadLocal<Int> = ThreadLocal()

    fun nextId(): Int {
        val value = columnNextId.get() ?: 0
        columnNextId.set(value + 1)
        return columnNextId.get()
    }

    fun reset() {
        columnNextId.set(0)
    }
}

typealias RowSelector<T> = (context: RowData<T>) -> Boolean