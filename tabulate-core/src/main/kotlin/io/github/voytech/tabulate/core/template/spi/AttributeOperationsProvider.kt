package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributeOperationsBuilder

typealias BuildAttributeOperations<CTX> = AttributeOperationsBuilder<CTX>.() -> Unit

interface AttributeOperationsProvider<CTX : RenderingContext, M : AbstractModel<M>> : RenderingContextAware<CTX>, ModelAware<M> {
    fun provideAttributeOperations(): BuildAttributeOperations<CTX>
}