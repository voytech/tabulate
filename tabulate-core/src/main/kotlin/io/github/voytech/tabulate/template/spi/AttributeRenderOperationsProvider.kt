package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.AttributeRenderOperationsFactory
import java.util.function.Predicate

interface AttributeRenderOperationsProvider<T, CTX: RenderingContext> : Predicate<Identifiable> {
    fun getAttributeOperationsFactory(renderingContext: CTX): AttributeRenderOperationsFactory<T>
}