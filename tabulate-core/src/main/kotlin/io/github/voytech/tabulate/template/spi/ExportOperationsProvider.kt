package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.ExportOperationsFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.ResultProvider
import java.util.function.Predicate

interface ExportOperationsProvider<T, CTX: RenderingContext> : ExportOperationsFactory<T>, Predicate<Identifiable>, Identifiable {
    fun createExportOperations(): TableExportOperations<T>
    fun getRenderingContext(): CTX
    fun createResultProviders(): List<ResultProvider<CTX>>
}