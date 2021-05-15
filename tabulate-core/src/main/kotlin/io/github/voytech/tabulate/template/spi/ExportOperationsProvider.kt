package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.operations.ExportOperations
import java.util.function.Predicate

interface ExportOperationsProvider<T, O> :  Predicate<Identifiable>, Identifiable {
    fun createOperations(): ExportOperations<T, O>
}