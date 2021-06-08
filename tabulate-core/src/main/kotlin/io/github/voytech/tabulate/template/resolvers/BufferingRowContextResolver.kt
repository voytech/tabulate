package io.github.voytech.tabulate.template.resolvers

class BufferingRowContextResolver<T> :
    AbstractRowContextResolver<T>() {

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
