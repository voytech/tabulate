package io.github.voytech.tabulate.pdf.components.sheet

import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.components.commons.operation.RenderPageOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext

class PdfSheetOperations: ExportOperationsProvider<PdfBoxRenderingContext, Page> {
    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(RenderPageOperation { renderingContext, _ ->
            renderingContext.addPage()
        })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Page> = reify()
}