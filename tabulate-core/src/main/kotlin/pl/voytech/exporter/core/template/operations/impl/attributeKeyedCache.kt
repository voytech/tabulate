package pl.voytech.exporter.core.template.operations.impl

import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.ContextData
import pl.voytech.exporter.core.template.operations.impl.AttributeKeyedCache.Companion.getCache

@Suppress("UNCHECKED_CAST")
class AttributeKeyedCache {
    private val rowExtToEntry: MutableMap<Set<RowAttribute>, MutableMap<String, Any>> = mutableMapOf()
    private val cellExtToEntry: MutableMap<Set<CellAttribute>, MutableMap<String, Any>> = mutableMapOf()
    private val collExtToEntry: MutableMap<Set<ColumnAttribute>, MutableMap<String, Any>> = mutableMapOf()

    fun getRowCacheEntry(attributes: Set<RowAttribute>): MutableMap<String, Any> {
        return rowExtToEntry[attributes] ?: kotlin.run {
            rowExtToEntry[attributes] = mutableMapOf(); rowExtToEntry[attributes]!!
        }
    }

    fun getColumnCacheEntry(attributes: Set<ColumnAttribute>): MutableMap<String, Any> {
        return collExtToEntry[attributes] ?: kotlin.run {
            collExtToEntry[attributes] = mutableMapOf(); collExtToEntry[attributes]!!
        }
    }

    fun getCellCacheEntry(attributes: Set<CellAttribute>): MutableMap<String, Any> {
        return cellExtToEntry[attributes] ?: kotlin.run {
            cellExtToEntry[attributes] = mutableMapOf(); cellExtToEntry[attributes]!!
        }
    }

    companion object {

        private const val ATTRIBUTES_CACHE_KEY = "_attributesCache"

        fun getCache(context: ContextData<*>): AttributeKeyedCache {
            context.additionalAttributes!!.putIfAbsent(ATTRIBUTES_CACHE_KEY, AttributeKeyedCache())
            return context.additionalAttributes!![ATTRIBUTES_CACHE_KEY] as AttributeKeyedCache
        }
    }
}

fun AttributedCell.getCachedValue(key: String): Any? {
    return this.attributes?.let {
        getCache(this).getCellCacheEntry(it)[key]
    }
}

fun AttributedCell.putCachedValue(key: String, value: Any): Any? {
    return this.attributes?.let {
        getCache(this).getCellCacheEntry(it).put(key, value)
    }
}

fun AttributedCell.putCachedValueIfAbsent(key: String, value: Any): Any {
    return this.attributes?.let {
        getCache(this).getCellCacheEntry(it).let { internalCache ->
            internalCache.putIfAbsent(key, value)
            internalCache[key]
        }
    } ?: value
}
