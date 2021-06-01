package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.Table

class BufferingRowContextResolver<T>(tableModel: Table<T>) :
    AbstractRowContextResolver<T>(tableModel) {

    private var index: Int = 0

    private val dataSourceBuffer: MutableList<T> = mutableListOf()

    override fun getNextRecord(): IndexedValue<T>? {
        return if (index < dataSourceBuffer.size) {
            IndexedValue(index, dataSourceBuffer[index]).also { index++ }
        } else null
    }

    fun buffer(record: T) {
        dataSourceBuffer.add(record)
    }

}
