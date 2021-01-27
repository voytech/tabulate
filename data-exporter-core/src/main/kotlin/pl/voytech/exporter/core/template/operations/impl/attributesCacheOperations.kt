package pl.voytech.exporter.core.template.operations.impl

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.TableOperations
import pl.voytech.exporter.core.template.operations.impl.AttributeKeyDrivenCache.Companion.ATTRIBUTES_CACHE_KEY

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

        fun putCellCachedValue(context: AttributedCell, key: String, value: Any): Any {
            (context.additionalAttributes?.get(ATTRIBUTES_CACHE_KEY) as MutableMap<String, Any>)[key] = value
            return getCellCachedValue(
                context,
                key
            )!!
        }

        fun getCellCachedValue(context: AttributedCell, key: String): Any? {
            return ((context.additionalAttributes?.get(ATTRIBUTES_CACHE_KEY) as MutableMap<String, Any>)[key])
        }

    }
}

class AttributeCacheTableOperations<T, A>(private val cache: AttributeKeyDrivenCache = AttributeKeyDrivenCache()) :
    TableOperations<T, A> {

    override fun createTable(state: A, table: Table<T>): Table<T>  = table

    override fun renderRow(
        state: A,
        context: AttributedRow<T>
    ) {
        context.rowAttributes?.let {
            context.additionalAttributes?.set(ATTRIBUTES_CACHE_KEY, cache.prepareRowCacheEntryScope(it))
        }
    }

    override fun renderColumn(state: A, context: AttributedColumn) {
        context.columnAttributes?.let { context.additionalAttributes?.set(ATTRIBUTES_CACHE_KEY,
            cache.prepareColumnCacheEntryScope(it)
        ) }
    }

    override fun renderRowCell(
        state: A,
        context: AttributedCell
    ) {
        context.attributes?.let { context.additionalAttributes?.set(ATTRIBUTES_CACHE_KEY,
            cache.prepareCellCacheEntryScope(it)
        ) }
    }

}
