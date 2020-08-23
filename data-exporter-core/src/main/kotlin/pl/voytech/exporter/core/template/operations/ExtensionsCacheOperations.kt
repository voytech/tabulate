package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.core.template.operations.ExtensionKeyDrivenCache.Companion.EXTENSIONS_CACHE_KEY

@Suppress("UNCHECKED_CAST")
class ExtensionKeyDrivenCache {
    private val rowExtToEntry: MutableMap<Set<RowExtension>, MutableMap<String, Any>> = mutableMapOf()
    private val cellExtToEntry: MutableMap<Set<CellExtension>, MutableMap<String, Any>> = mutableMapOf()
    private val collExtToEntry: MutableMap<Set<ColumnExtension>, MutableMap<String, Any>> = mutableMapOf()

    fun prepareRowCacheEntryScope(extensions: Set<RowExtension>): MutableMap<String, Any> {
        return rowExtToEntry[extensions] ?: kotlin.run {
            rowExtToEntry[extensions] = mutableMapOf(); rowExtToEntry[extensions]!!
        }
    }

    fun prepareColumnCacheEntryScope(extensions: Set<ColumnExtension>): MutableMap<String, Any> {
        return collExtToEntry[extensions] ?: kotlin.run {
            collExtToEntry[extensions] = mutableMapOf(); collExtToEntry[extensions]!!
        }
    }

    fun prepareCellCacheEntryScope(extensions: Set<CellExtension>): MutableMap<String, Any> {
        return cellExtToEntry[extensions] ?: kotlin.run {
            cellExtToEntry[extensions] = mutableMapOf(); cellExtToEntry[extensions]!!
        }
    }

    companion object {
        const val EXTENSIONS_CACHE_KEY = "extensionsCacheValue"

        fun <T> putCellCachedValue(context: OperationContext<T, CellOperationTableData<T>>, key: String, value: Any): Any {
            (context.additionalAttributes[EXTENSIONS_CACHE_KEY] as MutableMap<String, Any>)[key] = value
            return getCellCachedValue(
                context,
                key
            )!!
        }

        fun <T>  getCellCachedValue(context: OperationContext<T, CellOperationTableData<T>>, key: String): Any? {
            return ((context.additionalAttributes[EXTENSIONS_CACHE_KEY] as MutableMap<String, Any>)[key])
        }

    }
}

class ExtensionCacheTableOperations<T, A>(private val cache: ExtensionKeyDrivenCache = ExtensionKeyDrivenCache()) :
    TableOperations<T, A> {

    override fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>  = state

    override fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        extensions: Set<RowExtension>?
    ) {
        extensions?.let {
            context.additionalAttributes[EXTENSIONS_CACHE_KEY] = cache.prepareRowCacheEntryScope(it)
        }
    }

    override fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        extensions: Set<ColumnExtension>?
    ) {
        extensions?.let { context.additionalAttributes[EXTENSIONS_CACHE_KEY] = cache.prepareColumnCacheEntryScope(it) }
    }

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        extensions: Set<CellExtension>?
    ) {
        extensions?.let { context.additionalAttributes[EXTENSIONS_CACHE_KEY] = cache.prepareCellCacheEntryScope(it) }
    }

}
