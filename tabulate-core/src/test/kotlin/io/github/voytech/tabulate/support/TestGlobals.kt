package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.template.ContextResult
import io.github.voytech.tabulate.components.table.template.SuccessResult
import io.github.voytech.tabulate.core.DocumentFormat
import io.github.voytech.tabulate.core.ExportInstance
import io.github.voytech.tabulate.core.operation.ContextData
import io.github.voytech.tabulate.core.operation.factories.OperationsFactory

val spyDocumentFormat = DocumentFormat("spy")

val spyExportInstance: () -> ExportInstance = {
    ExportInstance(spyDocumentFormat,  OperationsFactory(spyDocumentFormat))
}


fun <CTX: ContextData> ContextResult<CTX>.success() : CTX = (this as SuccessResult<CTX>).context

