package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributeOperationsBuilder

typealias BuildAttributeOperations<CTX> = AttributeOperationsBuilder<CTX>.() -> Unit

interface AttributeOperationsProvider<CTX : RenderingContext, ARM : Model<ARM>> : RenderingContextAware<CTX>, ModelAware<ARM> {
    fun provideAttributeOperations(): BuildAttributeOperations<CTX>
}