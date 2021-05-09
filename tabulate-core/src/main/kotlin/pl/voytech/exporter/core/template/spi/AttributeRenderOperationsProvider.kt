package pl.voytech.exporter.core.template.spi

import pl.voytech.exporter.core.template.operations.AttributeRenderOperationsFactory
import java.util.function.Predicate

interface AttributeRenderOperationsProvider<CTX, T> : Predicate<Identifiable> {
    fun getAttributeOperationsFactory(creationContext: CTX): AttributeRenderOperationsFactory<T>
}