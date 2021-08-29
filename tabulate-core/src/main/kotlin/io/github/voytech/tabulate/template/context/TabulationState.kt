package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.iterators.OperationContextIterator
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver

/**
 * @author Wojciech Mąka
 * [TabulationState] keeps state separated from [TabulationTemplate] so that tabulate method invoked on [TabulationTemplate]
 * always starts with clear state from the beginning.
 * TabulationState manages following properties:
 * @property indexIncrement - a mutable composite index with custom markers (marker is a sub-index starting with
 * 0 value when created)
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
    val firstColumn: Int? = 0
) {
    private val stateAttributes = mutableMapOf<String, Any>()
    private val indexIncrement = MutableRowIndex()
    private val rowContextResolver: BufferingRowContextResolver<T> = BufferingRowContextResolver(tableModel, stateAttributes)
    private lateinit var rowContextIterator: OperationContextIterator<T, AttributedRow<T>>

    init {
        stateAttributes["_tableId"] = tableName
        createIterator()
    }

    fun mark(label: IndexLabel): RowIndex {
        return indexIncrement.mark(label.name).also {
            createIterator()
        }
    }

    fun bufferAndNext(record: T): AttributedRow<T>? {
        rowContextResolver.buffer(record)
        return next()
    }

    fun next(): AttributedRow<T>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

    private fun createIterator() {
        rowContextIterator = OperationContextIterator(rowContextResolver, indexIncrement)
    }

    internal fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

}
