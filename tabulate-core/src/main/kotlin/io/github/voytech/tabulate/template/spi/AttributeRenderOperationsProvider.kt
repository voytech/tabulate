package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.AttributeRenderOperationsFactory

interface AttributeRenderOperationsProvider<T, CTX: RenderingContext> {
    fun getAttributeOperationsFactory(renderingContext: CTX): AttributeRenderOperationsFactory<T>
    fun getContextClass(): Class<CTX>
}