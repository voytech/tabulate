package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes

abstract class TableDataSourceIndexedContextResolver<DS, T, CTX>(
    protected val tableModel: Table<T>,
    protected val stateAndAttributes: GlobalContextAndAttributes<T>
) :
    IndexedContextResolver<CTX> {
    var dataSource: DS? = null
}
