package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.ResultProvider

interface ExportOperationsProvider<T> : Identifiable {
    fun createExportOperations(): TableExportOperations<T>
    fun createResultProviders(): List<ResultProvider<*>>
}