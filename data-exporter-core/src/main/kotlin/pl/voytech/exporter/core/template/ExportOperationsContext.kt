package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Key

open class TableData<T>(private val collection: Collection<T>) {
    fun recordCount() = collection.size
}

class RowOperationTableData<T>(private val collection: Collection<T>) : TableData<T>(collection) {
    var rowValues: Map<Key<T>,CellValue?>?  = null
        internal set
}

class CellOperationTableData<T>(private val collection: Collection<T>): TableData<T>(collection) {
    var cellValue: CellValue? = null
        internal set
}

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

class ColumnOperationTableData<T>(private val collection: Collection<T>): TableData<T>(collection) {
    var columnValues: List<CellValue>? = null
        internal set
    var currentPhase: ColumnRenderPhase = ColumnRenderPhase.BEFORE_FIRST_ROW
}

class TableOperationTableData<T>(private val collection: Collection<T>): TableData<T>(collection) {
    fun getCollection(): Collection<T> {
        return collection
    }
}

data class OperationContext<T,E : TableData<T>>(
    val value: E,
    val additionalAttributes: MutableMap<String, Any>
) {
    var coordinates: Coordinates? = null
        internal set
}