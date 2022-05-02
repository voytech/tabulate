package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.RowEnd
import io.github.voytech.tabulate.core.template.TemplateContext

/**
 * Represents entire state produced throughout exporting process. State itself is separated from [TabulationTemplate].
 * [TabulationTemplate] creates [TypedTableTemplateContext] instance internally at every 'tabulate' method invocation so that
 * no additional state needs to be stored by [TabulationTemplate].
 * [TypedTableTemplateContext] manages following properties:
 * @property rowContextResolver - a strategy for transforming requested row index, table model, and collection elements
 * into [RowContextWithCells] which is used then by renderer.
 * @property rowContextIterator - iterates over elements and uses [RowContextResolver] in order to resolve [RowContextWithCells] for
 * requested index.
 * @property stateAttributes - map of generic attributes that may be shared during exporting.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
class TableTemplateContext<T : Any>(
    tableModel: Table<T>,
    stateAttributes: MutableMap<String, Any>,
    val dataSourceRecordClass: Class<T>? = null,
    val dataSource: Iterable<T>? = null
): TemplateContext<Table<T>>(tableModel,stateAttributes) {

    private lateinit var rowCompletionListener: RowCompletionListener<T>
    private lateinit var rowContextResolver: AccumulatingRowContextResolver<T>
    private lateinit var rowContextIterator: RowContextIterator<T>

    init {
        stateAttributes.computeIfAbsent("_sheetName") { tableModel.name }
    }

    internal fun initializeInternalState(rowCompletionListener: RowCompletionListener<T>) {
        rowContextResolver = AccumulatingRowContextResolver(model, stateAttributes, rowCompletionListener)
        rowContextIterator = RowContextIterator(rowContextResolver)
    }
    /**
     * Captures next element to be rendered at some point of time.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun capture(record: T): RowEnd<T>? {
        rowContextResolver.append(record)
        return next()
    }

    /**
     * Resolves next element.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun next(): RowEnd<T>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }
}
