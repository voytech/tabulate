package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.core.template.operations.AttributeKeyDrivenCache.Companion.ATTRIBUTES_CACHE_KEY

@Suppress("UNCHECKED_CAST")
class AttributeKeyDrivenCache {
    private val rowExtToEntry: MutableMap<Set<RowAttribute>, MutableMap<String, Any>> = mutableMapOf()
    private val cellExtToEntry: MutableMap<Set<CellAttribute>, MutableMap<String, Any>> = mutableMapOf()
    private val collExtToEntry: MutableMap<Set<ColumnAttribute>, MutableMap<String, Any>> = mutableMapOf()

    fun prepareRowCacheEntryScope(attributes: Set<RowAttribute>): MutableMap<String, Any> {
        return rowExtToEntry[attributes] ?: kotlin.run {
            rowExtToEntry[attributes] = mutableMapOf(); rowExtToEntry[attributes]!!
        }
    }

    fun prepareColumnCacheEntryScope(attributes: Set<ColumnAttribute>): MutableMap<String, Any> {
        return collExtToEntry[attributes] ?: kotlin.run {
            collExtToEntry[attributes] = mutableMapOf(); collExtToEntry[attributes]!!
        }
    }

    fun prepareCellCacheEntryScope(attributes: Set<CellAttribute>): MutableMap<String, Any> {
        return cellExtToEntry[attributes] ?: kotlin.run {
            cellExtToEntry[attributes] = mutableMapOf(); cellExtToEntry[attributes]!!
        }
    }

    companion object {
        const val ATTRIBUTES_CACHE_KEY = "extensionsCacheValue"

        fun <T> putCellCachedValue(context: OperationContext<T, CellOperationTableData<T>>, key: String, value: Any): Any {
            (context.additionalAttributes[ATTRIBUTES_CACHE_KEY] as MutableMap<String, Any>)[key] = value
            return getCellCachedValue(
                context,
                key
            )!!
        }

        fun <T>  getCellCachedValue(context: OperationContext<T, CellOperationTableData<T>>, key: String): Any? {
            return ((context.additionalAttributes[ATTRIBUTES_CACHE_KEY] as MutableMap<String, Any>)[key])
        }

    }
}

class AttributeCacheTableOperations<T, A>(private val cache: AttributeKeyDrivenCache = AttributeKeyDrivenCache()) :
    TableOperations<T, A> {

    override fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>  = state

    override fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        attributes: Set<RowAttribute>?
    ) {
        attributes?.let {
            context.additionalAttributes[ATTRIBUTES_CACHE_KEY] = cache.prepareRowCacheEntryScope(it)
        }
    }

    override fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        attributes: Set<ColumnAttribute>?
    ) {
        attributes?.let { context.additionalAttributes[ATTRIBUTES_CACHE_KEY] = cache.prepareColumnCacheEntryScope(it) }
    }

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        attributes: Set<CellAttribute>?
    ) {
        attributes?.let { context.additionalAttributes[ATTRIBUTES_CACHE_KEY] = cache.prepareCellCacheEntryScope(it) }
    }

}
