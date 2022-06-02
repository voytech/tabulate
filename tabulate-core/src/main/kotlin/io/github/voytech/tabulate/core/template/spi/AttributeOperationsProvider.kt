package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributesOperations

interface AttributeOperationsProvider<CTX : RenderingContext, ARM : Model<ARM>> : RenderingContextAware<CTX>, ModelAware<ARM> {
    fun createAttributeOperations(): AttributesOperations<CTX, ARM>
}