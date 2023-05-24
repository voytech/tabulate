package io.github.voytech.tabulate.pdf.components.page

import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.components.commons.operation.PageOperation
import io.github.voytech.tabulate.core.operation.Ok
import io.github.voytech.tabulate.core.operation.asResult
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.ExportOperationsProvider
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext

class PdfPageOperations: ExportOperationsProvider<PdfBoxRenderingContext, Page> {
    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(PageOperation { renderingContext, _ ->
            renderingContext.addPage()
        })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Page> = reify()
}