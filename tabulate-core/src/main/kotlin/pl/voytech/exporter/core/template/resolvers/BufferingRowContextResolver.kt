package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes

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
