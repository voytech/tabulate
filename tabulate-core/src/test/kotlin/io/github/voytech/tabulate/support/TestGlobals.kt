package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.CellContext
import io.github.voytech.tabulate.components.table.operation.RowEnd
import io.github.voytech.tabulate.components.table.operation.RowStart
import io.github.voytech.tabulate.components.table.template.CaptureRowCompletion
import io.github.voytech.tabulate.components.table.template.ContextResult
import io.github.voytech.tabulate.components.table.template.SuccessResult
import io.github.voytech.tabulate.components.table.template.TableTemplateContext
import io.github.voytech.tabulate.core.template.DocumentFormat
import io.github.voytech.tabulate.core.template.ExportTemplateServices
import io.github.voytech.tabulate.core.template.operation.ContextData
import io.github.voytech.tabulate.core.template.operation.OperationStatus
import io.github.voytech.tabulate.core.template.operation.Success
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory

val spyDocumentFormat = DocumentFormat("spy")

val spyExportTemplateServices: ExportTemplateServices  = ExportTemplateServices(
    spyDocumentFormat,
    ExportOperationsFactory(spyDocumentFormat),
)

fun <T: Any> Table<T>.createTableContext(customAttributes: MutableMap<String, Any>): TableTemplateContext<T> =
    TableTemplateContext(this,customAttributes, spyExportTemplateServices, emptyList())

fun <CTX: ContextData> ContextResult<CTX>.success() : CTX = (this as SuccessResult<CTX>).context

fun <T: Any> successfulRowComplete() = object: CaptureRowCompletion<T> {
    override fun onCellResolved(cell: CellContext): OperationStatus = Success
    override fun onRowStartResolved(row: RowStart): OperationStatus = Success
    override fun onRowEndResolved(row: RowEnd<T>): OperationStatus = Success
}