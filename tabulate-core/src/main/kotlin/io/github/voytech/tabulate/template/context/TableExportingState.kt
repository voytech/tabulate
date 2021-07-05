package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.NextId
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.iterators.OperationContextIterator
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver

/**
 * @author Wojciech Mąka
 */
class TableExportingState<T>(
    val tableModel: Table<T>,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0
) {
    private val stateAttributes = mutableMapOf<String, Any>()
    private val indexIncrement = MutableRowIndex()
    private val rowContextResolver: BufferingRowContextResolver<T> = BufferingRowContextResolver()
    private lateinit var rowContextIterator: OperationContextIterator<T, AttributedRow<T>>

    init {
        stateAttributes["_tableId"] = tableName
        createIterator()
    }

    fun incrementIndex()  = indexIncrement.inc()

    fun setRowIndex(index: Int) {
        indexIncrement.assign(index)
    }

    fun getRowIndex(): RowIndex = indexIncrement.getRowIndex()

    fun mark(label: IndexLabel): RowIndex {
        return indexIncrement.mark(label.name).also {
            createIterator()
        }
    }

    fun bufferAndNext(record: T): AttributedRow<T>? {
        rowContextResolver.buffer(record)
        return getNextRowContext()
    }

    private fun createIterator() {
        rowContextIterator = OperationContextIterator(rowContextResolver)
        rowContextIterator.setState(this)
    }

    fun getNextRowContext(): AttributedRow<T>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

    internal fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

}
