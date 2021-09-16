package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.iterators.EnumStepProvider
import io.github.voytech.tabulate.template.iterators.OperationContextIterator
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver

/**
 * @author Wojciech Mąka
 * [TabulationState] keeps state separated from [TabulationTemplate] so that tabulate method invoked on [TabulationTemplate]
 * always starts with clear state from the beginning.
 * TabulationState manages following properties:
 * @property rowContextResolver - strategy for transforming current index, table model, and current record into [RowContext]
 * which is used then by rendering operations and delegate rendering context (e.g. third party API like Apache POI)
 * @property rowContextIterator - iterates over index and uses [RowContextResolver] in order to resolve [RowContext] for
 * current index.
 * @property stateAttributes - generic map of attributes that may be shared globally within operation implementors. It
 * is a shared property within [RowContext], [ColumnContext], [RowCellContext].
 */
class TabulationState<T>(
    val tableModel: Table<T>,
    val tableName: String = "untitled table",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
) {
    private val stateAttributes = mutableMapOf<String, Any>()
    private val rowContextResolver: BufferingRowContextResolver<T> =
        BufferingRowContextResolver(tableModel, stateAttributes)
    private val rowContextIterator: OperationContextIterator<T, AttributedRow<T>> =
        OperationContextIterator(rowContextResolver, EnumStepProvider(DefaultSteps::class.java))

    init {
        stateAttributes["_tableId"] = tableName
    }

    fun mark(label: DefaultSteps): RowIndex {
        return rowContextIterator.mark(label.name)
    }

    fun bufferAndNext(record: T): AttributedRow<T>? {
        rowContextResolver.buffer(record)
        return next()
    }

    fun next(): AttributedRow<T>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

    internal fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

}
