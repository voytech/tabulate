package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes
import pl.voytech.exporter.core.template.context.RowOperationContext

abstract class TableDataSourceContextResolver<DS, T>(
    protected val tableModel: Table<T>,
    protected val stateAndAttributes: GlobalContextAndAttributes<T>
) :
    IndexedContextResolver<T, RowOperationContext<T>> {
    var dataSource: DS? = null
}
