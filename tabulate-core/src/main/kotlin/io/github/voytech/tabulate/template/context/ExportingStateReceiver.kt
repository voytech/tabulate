package io.github.voytech.tabulate.template.context

interface ExportingStateReceiver<T> {
    fun setState(exportingState: TableExportingState<T>)
}