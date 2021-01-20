package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.Table

abstract class TableDataSourceIndexedContextResolver<DS, T, CTX>(protected val tableModel: Table<T>) :
    IndexedContextResolver<CTX> {
    var dataSource: DS? = null
}
