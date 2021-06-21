package io.github.voytech.tabulate.template.resolvers

import java.util.*

class BufferingRowContextResolver<T> : AbstractRowContextResolver<T>() {

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
