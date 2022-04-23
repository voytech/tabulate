package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.RowClosingContext
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
class TableTemplateContext(
    tableModel: Table<*>,
    stateAttributes: MutableMap<String, Any>,
    val dataSourceRecordClass: Class<*>? = null,
    private val dataSource: Iterable<*>? = null
): TemplateContext<Table<*>>(tableModel,stateAttributes) {
    @Suppress("UNCHECKED_CAST")
    fun <T> getTyped(clazz: Class<T>, rowCompletionListener: RowCompletionListener<T>): TypedTableTemplateContext<T> = TypedTableTemplateContext(
        tableModel = model as Table<T>,
        stateAttributes = stateAttributes,
        rowCompletionListener = rowCompletionListener,
        dataSource = dataSource as? Iterable<T>
    )
}

class TypedTableTemplateContext<T>(
    tableModel: Table<T>,
    stateAttributes: MutableMap<String, Any>,
    rowCompletionListener: RowCompletionListener<T>? = null,
    val dataSource: Iterable<T>? = null
): TemplateContext<Table<T>>(tableModel, stateAttributes) {

    private val rowContextResolver: AccumulatingRowContextResolver<T> =
        AccumulatingRowContextResolver(tableModel, stateAttributes, rowCompletionListener)
    private val rowContextIterator: RowContextIterator<T> =
        RowContextIterator(rowContextResolver)

    init {
        stateAttributes.computeIfAbsent("_sheetName") { tableModel.name }
    }

    /**
     * Captures next element to be rendered at some point of time.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun capture(record: T): RowClosingContext<T>? {
        rowContextResolver.append(record)
        return next()
    }

    /**
     * Resolves next element.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun next(): RowClosingContext<T>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

}
