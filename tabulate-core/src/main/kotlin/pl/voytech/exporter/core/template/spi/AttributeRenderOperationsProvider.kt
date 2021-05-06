package pl.voytech.exporter.core.template.spi

import pl.voytech.exporter.core.template.operations.AttributeRenderOperationsFactory
import java.util.function.Predicate

interface AttributeRenderOperationsProvider<T> : AttributeRenderOperationsFactory<T>, Predicate<Identifiable>