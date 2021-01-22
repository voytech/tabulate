package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes

class RowContextResolver<T>(tableModel: Table<T>, stateAndAttributes: GlobalContextAndAttributes<T>, collection: Collection<T>) :
    AbstractRowContextResolver<Collection<T>, T>(tableModel, stateAndAttributes) {

    private var iterator: Iterator<T>? = null

    private var index: Int = 0

    init {
        dataSource = collection
    }

    private fun withIterator(): Iterator<T>? {
        if (dataSource != null && iterator == null) {
            iterator = dataSource!!.iterator()
        }
        return iterator
    }

    override fun getNextRecord(): IndexedValue<T>? {
        with(withIterator()) {
            return if (this?.hasNext() == true) IndexedValue(index++, this.next()) else null
        }
    }

}
