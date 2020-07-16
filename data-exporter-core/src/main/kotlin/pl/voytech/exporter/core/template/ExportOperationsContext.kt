package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Key
import kotlin.reflect.KProperty1

open class TableDataContext<T>(private val collection: Collection<T>) {
    private var cached: Array<KProperty1<T, *>>? = null

    fun recordCount() = collection.size

    fun recordProperties(): Array<KProperty1<T, *>>? {
        assert(collection!!.isNotEmpty())
        return null //collection!!.first()!!::class!!.members!!.filterIsInstance<KProperty1<T, *>>()!!.toTypedArray()!!
    }
}

class RowOperationTableDataContext<T>(private val collection: Collection<T>) : TableDataContext<T>(collection) {
    var rowValues: Map<Key<T>,CellValue?>?  = null
        internal set
}

class CellOperationTableDataContext<T>(private val collection: Collection<T>): TableDataContext<T>(collection) {
    var cellValue: CellValue? = null
        internal set
}

class ColumnOperationTableDataContext<T>(private val collection: Collection<T>): TableDataContext<T>(collection) {
    var columnValues: List<CellValue>? = null
        internal set
}

class TableOperationTableDataContext<T>(private val collection: Collection<T>): TableDataContext<T>(collection) {
    fun getCollection(): Collection<T> {
        return collection
    }
}

data class OperationContext<T,E : TableDataContext<T>>(
    val data: E
) {
    var coordinates: Coordinates? = null
        internal set
}