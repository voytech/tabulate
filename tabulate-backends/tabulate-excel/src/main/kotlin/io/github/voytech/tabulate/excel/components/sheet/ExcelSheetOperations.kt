package io.github.voytech.tabulate.excel.components.sheet

import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.components.commons.operation.PageOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.ExportOperationsProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext

class ExcelSheetOperations: ExportOperationsProvider<ApachePoiRenderingContext, Page> {

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(PageOperation { renderingContext, context ->
            renderingContext.provideWorkbook()
            renderingContext.provideSheet(context.pageName)
        })
    }

    override fun getModelClass(): Class<Page> = reify()

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> = DocumentFormat.format("xlsx", "poi")
}