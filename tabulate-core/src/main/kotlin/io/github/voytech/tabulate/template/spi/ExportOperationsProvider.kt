package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.operations.ExportOperationsFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations
import java.util.function.Predicate


interface ExportOperationsProvider<T, O> : ExportOperationsFactory<T, O>, Predicate<Identifiable>, Identifiable {
    fun create(): TableExportOperations<T, O>
}