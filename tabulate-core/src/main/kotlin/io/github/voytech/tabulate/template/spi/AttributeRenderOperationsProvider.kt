package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.operations.AttributeRenderOperationsFactory
import java.util.function.Predicate

interface AttributeRenderOperationsProvider<CTX, T> : Predicate<Identifiable> {
    fun getAttributeOperationsFactory(creationContext: CTX): AttributeRenderOperationsFactory<T>
}