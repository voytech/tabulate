package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.rendering.CellRenderable
import io.github.voytech.tabulate.components.table.rendering.RowEndRenderable
import io.github.voytech.tabulate.components.table.rendering.RowStartRenderable
import io.github.voytech.tabulate.components.table.template.CaptureRowCompletion
import io.github.voytech.tabulate.components.table.template.ContextResult
import io.github.voytech.tabulate.components.table.template.SuccessResult
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.DocumentFormat
import io.github.voytech.tabulate.core.ExportInstance
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.core.operation.factories.OperationsFactory

val spyDocumentFormat = DocumentFormat("spy")

val spyExportInstance: () -> ExportInstance = {
    ExportInstance(spyDocumentFormat,  OperationsFactory(spyDocumentFormat))
}

fun <T: Any> Table<T>.createTableContext(customAttributes: MutableMap<String, Any>): ModelExportContext = with(spyExportInstance()) {
    createStandaloneExportContext(StateAttributes(customAttributes))
}


fun <CTX: ContextData> ContextResult<CTX>.success() : CTX = (this as SuccessResult<CTX>).context

