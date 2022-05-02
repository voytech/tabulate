package io.github.voytech.tabulate.excel.components.sheet

import io.github.voytech.tabulate.components.sheet.model.Sheet
import io.github.voytech.tabulate.components.sheet.operation.RenderSheetOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext

class ExcelSheetOperations: ExportOperationsFactory<ApachePoiRenderingContext, Sheet>() {

    override fun provideExportOperations(): OperationsBuilder<ApachePoiRenderingContext, Sheet>.() -> Unit = {
        operation(RenderSheetOperation {renderingContext, context ->
            renderingContext.provideWorkbook()
            renderingContext.provideSheet(context.sheetName)
        })
    }

    override fun getModelClass(): Class<Sheet> = reify()

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> = DocumentFormat.format("xlsx", "poi")
}