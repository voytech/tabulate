package io.github.voytech.tabulate.pdf.components.sheet

import io.github.voytech.tabulate.components.sheet.model.Sheet
import io.github.voytech.tabulate.components.sheet.operation.RenderSheetOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext

class PdfSheetOperations: ExportOperationsFactory<PdfBoxRenderingContext, Sheet>() {
    override fun provideExportOperations(): OperationsBuilder<PdfBoxRenderingContext, Sheet>.() -> Unit = {
        operation(RenderSheetOperation { renderingContext, _ ->
            renderingContext.addPage()
        })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Sheet> = reify()
}