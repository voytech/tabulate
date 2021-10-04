package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.iterators.EnumStepProvider
import io.github.voytech.tabulate.template.iterators.RowContextIterator
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver
import io.github.voytech.tabulate.template.resolvers.RowCompletionNotifier

/**
 * @author Wojciech Mąka
 * [TabulationState] keeps state separated from [TabulationTemplate] so that tabulate method invoked on [TabulationTemplate]
 * always starts with clear state from the beginning.
 * TabulationState manages following properties:
 * @property rowContextResolver - strategy for transforming current index, table model, and current record into [RowContextWithCells]
 * which is used then by rendering operations and delegate rendering context (e.g. third party API like Apache POI)
 * @property rowContextIterator - iterates over index and uses [RowContextResolver] in order to resolve [RowContextWithCells] for
 * current index.
 * @property stateAttributes - generic map of attributes that may be shared globally within operation implementors. It
 * is a shared property within [RowContextWithCells], [ColumnContext], [RowCellContext].
 */
internal class TabulationState<T>(
    val tableModel: Table<T>,
    val tableName: String = "untitled table",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
    var rowCompletionNotifier: RowCompletionNotifier<T>? = null,
) {
    private val stateAttributes = mutableMapOf<String, Any>()
    private val rowContextResolver: BufferingRowContextResolver<T> =
        BufferingRowContextResolver(tableModel, stateAttributes, rowCompletionNotifier)
    private val rowContextIterator: RowContextIterator<T, AttributedRowWithCells<T>> =
        RowContextIterator(rowContextResolver, EnumStepProvider(DefaultSteps::class.java))

    init {
        stateAttributes["_tableId"] = tableName
    }

    fun mark(label: DefaultSteps): RowIndex {
        return rowContextIterator.mark(label.name)
    }

    fun bufferAndNext(record: T): AttributedRowWithCells<T>? {
        rowContextResolver.buffer(record)
        return next()
    }

    fun next(): AttributedRowWithCells<T>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

    internal fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

}
