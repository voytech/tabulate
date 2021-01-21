package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.StateAndContext

class RowContextResolver<T>(tableModel: Table<T>, state: StateAndContext<T>, collection: Collection<T>) :
    AbstractRowContextResolver<Collection<T>, T>(tableModel, state) {

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
