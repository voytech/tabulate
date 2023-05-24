package io.github.voytech.tabulate.core.spi

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.operation.AttributeOperationsBuilder

typealias BuildAttributeOperations<CTX> = AttributeOperationsBuilder<CTX>.() -> Unit

interface AttributeOperationsProvider<CTX : RenderingContext, M : AbstractModel> : RenderingContextAware<CTX>,
    ModelAware<M> {
    fun provideAttributeOperations(): BuildAttributeOperations<CTX>
}