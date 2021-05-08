package pl.voytech.exporter.core.template.spi

import pl.voytech.exporter.core.template.operations.ExportOperations
import pl.voytech.exporter.core.template.operations.ExportOperationsFactory
import java.util.function.Predicate

interface ExportOperationsProvider<CTX, T, O> :  ExportOperationsFactory<CTX, T, O>, Predicate<Identifiable>, Identifiable {
    fun getFactoryContext(): CTX
    fun createOperations(): ExportOperations<T, O>
}