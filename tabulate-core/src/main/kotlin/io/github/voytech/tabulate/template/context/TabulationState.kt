package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.iterators.RowContextIterator
import io.github.voytech.tabulate.template.operations.RowClosingContext
import io.github.voytech.tabulate.template.resolvers.AccumulatingRowContextResolver
import io.github.voytech.tabulate.template.resolvers.RowCompletionListener

/**
 * Represents entire state produced throughout exporting process. State itself is separated from [TabulationTemplate].
 * [TabulationTemplate] creates [TabulationState] instance internally at every 'tabulate' method invocation so that
 * no additional state needs to be stored by [TabulationTemplate].
 * [TabulationState] manages following properties:
 * @property rowContextResolver - a strategy for transforming requested row index, table model, and collection elements
 * into [RowContextWithCells] which is used then by renderer.
 * @property rowContextIterator - iterates over elements and uses [RowContextResolver] in order to resolve [RowContextWithCells] for
 * requested index.
 * @property stateAttributes - map of generic attributes that may be shared during exporting.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
internal class TabulationState<T>(
    val renderingContext: RenderingContext,
    val tableModel: Table<T>,
    var rowCompletionListener: RowCompletionListener<T>? = null,
) {
    private val stateAttributes = mutableMapOf<String, Any>()
    private val rowContextResolver: AccumulatingRowContextResolver<T> =
        AccumulatingRowContextResolver(tableModel, stateAttributes, rowCompletionListener)
    private val rowContextIterator: RowContextIterator<T> =
        RowContextIterator(rowContextResolver)

    init {
        stateAttributes["_tableId"] = tableModel.name
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

    /**
     * Returns custom attributes.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    internal fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

}
