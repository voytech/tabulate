package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.context.AttributedRow
import java.util.*

/**
 * Given requested index, [Table] model, and global custom attributes, it resolves [AttributedRow] context data with
 * effective index (effective index may differ from requested one if there is no rows matching predicate matching requested index)
 * It extends [AbstractRowContextResolver] by adding buffering of rows from source collection as long as it is not always
 * the case that current collection record is the one that is transformed to table row.
 * @author Wojciech Mąka
 */
class BufferingRowContextResolver<T>(
    tableModel: Table<T>,
    customAttributes: MutableMap<String, Any>
) : AbstractRowContextResolver<T>(tableModel, customAttributes) {

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
