package pl.voytech.exporter.core.template.operations.chain

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.core.template.RowOperation
import pl.voytech.exporter.core.template.operations.chain.ExtensionsCacheOperationsFactory.EXTENSIONS_CACHE_KEY

class ExtensionKeyDrivenCache {
    private val rowExtToEntry: MutableMap<Set<RowExtension>, MutableMap<String, Any>> = mutableMapOf()
    private val cellExtToEntry: MutableMap<Set<CellExtension>, MutableMap<String, Any>> = mutableMapOf()
    private val collExtToEntry: MutableMap<Set<ColumnExtension>, MutableMap<String, Any>> = mutableMapOf()

    fun rowEntry(extensions: Set<RowExtension>): MutableMap<String, Any> {
        return rowExtToEntry[extensions] ?: kotlin.run {
            rowExtToEntry[extensions] = mutableMapOf(); rowExtToEntry[extensions]!!
        }
    }

    fun columnEntry(extensions: Set<ColumnExtension>): MutableMap<String, Any> {
        return collExtToEntry[extensions] ?: kotlin.run {
            collExtToEntry[extensions] = mutableMapOf(); collExtToEntry[extensions]!!
        }
    }

    fun cellEntry(extensions: Set<CellExtension>): MutableMap<String, Any> {
        return cellExtToEntry[extensions] ?: kotlin.run {
            cellExtToEntry[extensions] = mutableMapOf(); cellExtToEntry[extensions]!!
        }
    }
}

object ExtensionsCacheOperationsFactory {

    const val EXTENSIONS_CACHE_KEY = "extensionsCacheValue"

    private val cache: ExtensionKeyDrivenCache = ExtensionKeyDrivenCache()

    fun <T, A> extensionCacheRowOperation(): ExtensionCacheRowOperation<T, A> {
        return ExtensionCacheRowOperation(cache)
    }

    fun <T, A> extensionCacheRowCellOperation(): ExtensionCacheRowCellOperation<T, A> {
        return ExtensionCacheRowCellOperation(cache)
    }

    fun <T, A> extensionCacheColumnOperation(): ExtensionCacheColumnOperation<T, A> {
        return ExtensionCacheColumnOperation(cache)
    }
}

class ExtensionCacheRowOperation<T, A> internal constructor(private val cache: ExtensionKeyDrivenCache) :
    RowOperation<T, A> {

    override fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        extensions: Set<RowExtension>?
    ) {
        extensions?.let {
            context.additionalAttributes[EXTENSIONS_CACHE_KEY] = cache.rowEntry(it)
        }
    }

}

class ExtensionCacheColumnOperation<T, A> internal constructor(private val cache: ExtensionKeyDrivenCache) :
    ColumnOperation<T, A> {

    override fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        extensions: Set<ColumnExtension>?
    ) {
        extensions?.let { context.additionalAttributes[EXTENSIONS_CACHE_KEY] = cache.columnEntry(it) }
    }

    override fun beforeFirstRow(): Boolean = true

    override fun afterLastRow(): Boolean = false

}

class ExtensionCacheRowCellOperation<T, A> internal constructor(private val cache: ExtensionKeyDrivenCache) :
    RowCellOperation<T, A> {

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        extensions: Set<CellExtension>?
    ) {
        extensions?.let { context.additionalAttributes[EXTENSIONS_CACHE_KEY] = cache.cellEntry(it) }
    }

}