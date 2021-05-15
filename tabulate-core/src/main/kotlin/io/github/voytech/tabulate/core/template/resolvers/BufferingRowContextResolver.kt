package io.github.voytech.tabulate.core.template.resolvers

import io.github.voytech.tabulate.core.model.Table
import io.github.voytech.tabulate.core.template.context.GlobalContextAndAttributes

class BufferingRowContextResolver<T>(tableModel: Table<T>, stateAndAttributes: GlobalContextAndAttributes<T>) :
    AbstractRowContextResolver<T>(tableModel, stateAndAttributes) {

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
