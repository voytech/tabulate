package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.GlobalContextAndAttributes

abstract class GlobalStateAwareContextResolver<T>(
    protected val tableModel: Table<T>,
    protected val stateAndAttributes: GlobalContextAndAttributes<T>
) : IndexedContextResolver<T, AttributedRow<T>>
