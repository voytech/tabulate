package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.context.*
import io.github.voytech.tabulate.template.operations.AttributeSetCache.Companion.getCache

@Suppress("UNCHECKED_CAST")
class AttributeSetCache {
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

        fun getCache(context: ContextData): AttributeSetCache {
            context.additionalAttributes!!.putIfAbsent(ATTRIBUTES_CACHE_KEY, AttributeSetCache())
            return context.additionalAttributes!![ATTRIBUTES_CACHE_KEY] as AttributeSetCache
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

fun <T> AttributedRowWithCells<T>.ensureAttributesCacheEntry() {
    this.rowAttributes?.let {
        getCache(this).getRowCacheEntry(it)
    }?.also{
        additionalAttributes?.put("_currentRowAttributesCache", it)
    }
}

fun AttributedColumn.ensureAttributesCacheEntry() {
    this.columnAttributes?.let {
        getCache(this).getColumnCacheEntry(it)
    }?.also{
        additionalAttributes?.put("_currentColumnAttributesCache", it)
    }
}


@Suppress("UNCHECKED_CAST")
fun RowCellContext.putCachedValueIfAbsent(key: String, value: Any): Any {
    return (this.additionalAttributes?.get("_currentCellAttributesCache") as MutableMap<String, Any>).let {
        it.computeIfAbsent(key){ value }
    }
}
