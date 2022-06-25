package io.github.voytech.tabulate.pdf.components.sheet

import io.github.voytech.tabulate.components.sheet.model.Sheet
import io.github.voytech.tabulate.components.sheet.operation.RenderSheetOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext

class PdfSheetOperations: ExportOperationsProvider<PdfBoxRenderingContext, Sheet> {
    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(RenderSheetOperation { renderingContext, _ ->
            renderingContext.addPage()
        })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Sheet> = reify()
}