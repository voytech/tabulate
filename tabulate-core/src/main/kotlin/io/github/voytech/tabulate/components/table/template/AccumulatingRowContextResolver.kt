package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.model.StateAttributes
import java.util.*

/**
 * It extends [AbstractRowContextResolver] by adding collection elements buffering to support case when current record
 * does not satisfy row predicates for current index and needs to be rendered eventually.
 * @see AbstractRowContextResolver
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class AccumulatingRowContextResolver<T: Any>(
    tableModel: Table<T>,
    state: StateAttributes,
    continuations: TableContinuations,
    listener: CaptureRowCompletion<T>
) : AbstractRowContextResolver<T>(tableModel, state, continuations, listener) {

    private var pollIndex: Int = 0
    private val dataSourceBuffer: Queue<T> = LinkedList()

    override fun getNextRecord(): IndexedValue<T>? {
        return dataSourceBuffer.poll()?.let {
            IndexedValue(pollIndex, it).also { pollIndex++ }
        }
    }

    fun append(record: T) {
        dataSourceBuffer.add(record)
    }

}
