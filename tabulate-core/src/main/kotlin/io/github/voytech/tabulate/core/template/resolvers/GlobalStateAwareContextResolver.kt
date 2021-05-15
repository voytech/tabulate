package io.github.voytech.tabulate.core.template.resolvers

import io.github.voytech.tabulate.core.model.Table
import io.github.voytech.tabulate.core.template.context.AttributedRow
import io.github.voytech.tabulate.core.template.context.GlobalContextAndAttributes

abstract class GlobalStateAwareContextResolver<T>(
    protected val tableModel: Table<T>,
    protected val stateAndAttributes: GlobalContextAndAttributes<T>
) : IndexedContextResolver<T, AttributedRow<T>>
