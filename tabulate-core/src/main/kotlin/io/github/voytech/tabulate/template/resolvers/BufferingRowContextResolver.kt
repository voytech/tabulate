package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.Table
import java.util.*

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
