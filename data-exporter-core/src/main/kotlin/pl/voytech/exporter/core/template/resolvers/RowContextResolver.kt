package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.Table

class RowContextResolver<T>(tableModel: Table<T>) :
    AbstractRowContextResolver<Collection<T>, T>(
        tableModel
    ) {
    private var iterator: Iterator<T>? = null

    private fun withIterator(): Iterator<T>? {
        if (dataSource != null && iterator == null) {
            iterator = dataSource!!.iterator()
        }
        return iterator
    }

    override fun getNextDataSourceRecord(): T {
        TODO("Not yet implemented")
    }


}
