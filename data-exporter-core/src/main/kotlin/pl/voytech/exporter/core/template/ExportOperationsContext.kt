package pl.voytech.exporter.core.template

import kotlin.reflect.KProperty1

open class CollectionAccessor<T>(private val collection: Collection<T>) {
  private var cached: Array<KProperty1<T,*>>? = null

  fun recordCount() = collection.size
  fun recordProperties(): Array<KProperty1<T,*>> {
      assert(collection.isNotEmpty())
      return if (cached == null) {
          cached = collection.first()!!::class.members.filterIsInstance<KProperty1<T,*>>().toTypedArray()
          cached!!
      } else {
          cached!!
      }
  }
}

class RowOperationCollectionAccessor<T>(private val collection: Collection<T>) : CollectionAccessor<T>(collection) {
    fun getRecordAtRow(rowIndex: Int): T = collection.filterIndexed { index, _ ->  index == rowIndex}.first()
}

class ColumnOperationCollectionAccessor<T>(private val collection: Collection<T>): CollectionAccessor<T>(collection) {
    private val cached: MutableMap<KProperty1<T,*>,List<*>> = mutableMapOf()

    fun getRecordsAtColumn(columnIndex: Int): List<*> {
        val property: KProperty1<T,*> = recordProperties()[columnIndex]
        return when {
            !cached.containsKey(property) ->
                cached.putIfAbsent(property, collection.map { rec -> property.get(rec) })!!
            else -> cached[property]!!
        }
    }
}

class TableOperationCollectionAccessor<T>(private val collection: Collection<T>): CollectionAccessor<T>(collection) {
    fun getCollection(): Collection<T> {
        return collection
    }
}

data class OperationContext<T,E : CollectionAccessor<T>>(
    val dataAccessor: E,
    val coordinates: Coordinates
)