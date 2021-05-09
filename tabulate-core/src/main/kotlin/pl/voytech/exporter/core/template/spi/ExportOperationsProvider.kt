package pl.voytech.exporter.core.template.spi

import pl.voytech.exporter.core.template.operations.ExportOperations
import java.util.function.Predicate

interface ExportOperationsProvider<T, O> :  Predicate<Identifiable>, Identifiable {
    fun createOperations(): ExportOperations<T, O>
}