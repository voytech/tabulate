package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.Table
import java.util.*

/**
 * It extends [AbstractRowContextResolver] by adding collection elements buffering to support case when current record
 * does not satisfy row predicates for current index and needs to be rendered eventually.
 * @see AbstractRowContextResolver
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class AccumulatingRowContextResolver<T>(
    tableModel: Table<T>,
    customAttributes: MutableMap<String, Any>,
    listener: RowCompletionListener<T>? = null
) : AbstractRowContextResolver<T>(tableModel, customAttributes, listener) {

    private var index: Int = 0

    private val dataSourceBuffer: Queue<T> = LinkedList()

    override fun getNextRecord(): IndexedValue<T>? {
        return dataSourceBuffer.poll()?.let {
            IndexedValue(index, it).also { index++ }
        }
    }

    fun append(record: T) {
        dataSourceBuffer.add(record)
    }

}
