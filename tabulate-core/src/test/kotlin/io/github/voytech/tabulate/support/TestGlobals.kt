package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.CellContext
import io.github.voytech.tabulate.components.table.operation.RowEnd
import io.github.voytech.tabulate.components.table.operation.RowStart
import io.github.voytech.tabulate.components.table.template.CaptureRowCompletion
import io.github.voytech.tabulate.components.table.template.ContextResult
import io.github.voytech.tabulate.components.table.template.SuccessResult
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.template.DocumentFormat
import io.github.voytech.tabulate.core.template.ExportInstance
import io.github.voytech.tabulate.core.template.operation.ContextData
import io.github.voytech.tabulate.core.template.operation.OperationResult
import io.github.voytech.tabulate.core.template.operation.Success
import io.github.voytech.tabulate.core.template.operation.factories.OperationsFactory

val spyDocumentFormat = DocumentFormat("spy")

val spyExportInstance: ExportInstance  = ExportInstance(
    spyDocumentFormat,
    OperationsFactory(spyDocumentFormat),
)

fun <T: Any> Table<T>.createTableContext(customAttributes: MutableMap<String, Any>): ModelExportContext<Table<T>> =
    ModelExportContext(this, StateAttributes(customAttributes), spyExportInstance)

fun <CTX: ContextData> ContextResult<CTX>.success() : CTX = (this as SuccessResult<CTX>).context

fun <T: Any> successfulRowComplete() = object: CaptureRowCompletion<T> {
    override fun onCellResolved(cell: CellContext): OperationResult = Success
    override fun onRowStartResolved(row: RowStart): OperationResult = Success
    override fun onRowEndResolved(row: RowEnd<T>): OperationResult = Success
}