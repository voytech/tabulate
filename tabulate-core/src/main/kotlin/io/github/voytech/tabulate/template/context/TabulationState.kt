package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.iterators.EnumStepProvider
import io.github.voytech.tabulate.template.iterators.RowContextIterator
import io.github.voytech.tabulate.template.operations.AttributedRowWithCells
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver
import io.github.voytech.tabulate.template.resolvers.RowCompletionListener

/**
 * [TabulationState] keeps state separated from [TabulationTemplate] so that tabulate method invoked on [TabulationTemplate]
 * always starts with clear state from the beginning.
 * TabulationState manages following properties:
 * @property rowContextResolver - strategy for transforming current index, table model, and current collection element
 * into [RowContextWithCells] which is used then by rendering operations and delegate rendering context (e.g. third party API like Apache POI)
 * @property rowContextIterator - iterates over elements and uses [RowContextResolver] in order to resolve [RowContextWithCells] for
 * requested index.
 * @property stateAttributes - generic map of attributes that may be shared globally within operation implementors.
 * It is a property shared across: [RowContextWithCells], [ColumnContext], [RowCellContext].
 * @author Wojciech Mąka
 */
internal class TabulationState<T>(
    val tableModel: Table<T>,
    val tableName: String = "untitled table",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
    var rowCompletionListener: RowCompletionListener<T>? = null,
) {
    private val stateAttributes = mutableMapOf<String, Any>()
    private val rowContextResolver: BufferingRowContextResolver<T> =
        BufferingRowContextResolver(tableModel, stateAttributes, rowCompletionListener)
    private val rowContextIterator: RowContextIterator<T> =
        RowContextIterator(rowContextResolver, EnumStepProvider(AdditionalSteps::class.java))

    init {
        stateAttributes["_tableId"] = tableName
    }

    fun mark(label: AdditionalSteps): RowIndex {
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
