package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.Table
import java.util.*

/**
 * Given requested index, [Table] model, and global custom attributes, it resolves [AttributedRowWithCells] context data with
 * effective index (effective index may differ from requested one if there are no rows matching predicate
 * - in that case - row context with next matching index is returned).
 * It extends [AbstractRowContextResolver] by adding row buffering to support case when current record does not satisfy
 * row predicates, but will be rendered eventually.
 * @author Wojciech Mąka
 */
internal class BufferingRowContextResolver<T>(
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

    fun buffer(record: T) {
        dataSourceBuffer.add(record)
    }

}
