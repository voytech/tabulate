package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes

abstract class GlobalStateAwareContextResolver<T>(
    protected val tableModel: Table<T>,
    protected val stateAndAttributes: GlobalContextAndAttributes<T>
) : IndexedContextResolver<T, AttributedRow<T>>
