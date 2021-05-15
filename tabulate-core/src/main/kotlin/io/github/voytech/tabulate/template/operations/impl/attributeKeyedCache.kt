package io.github.voytech.tabulate.template.operations.impl

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.ContextData
import io.github.voytech.tabulate.template.context.RowCellContext
import io.github.voytech.tabulate.template.operations.impl.AttributeKeyedCache.Companion.getCache

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

fun AttributedCell.ensureAttributesCacheEntry() {
    this.attributes?.let {
        getCache(this).getCellCacheEntry(it)
    }?.also{
        additionalAttributes?.put("_currentCellAttributesCache", it)
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

@Suppress("UNCHECKED_CAST")
fun RowCellContext.putCachedValueIfAbsent(key: String, value: Any): Any {
    return (this.additionalAttributes?.get("_currentCellAttributesCache") as MutableMap<String, Any>).let {
        it.computeIfAbsent(key){ value }
    }
}
