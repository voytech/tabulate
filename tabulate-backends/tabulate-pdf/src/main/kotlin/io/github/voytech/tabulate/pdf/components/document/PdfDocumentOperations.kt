package io.github.voytech.tabulate.pdf.components.document

import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.components.document.operation.TurnPageOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext


class PdfDocumentOperations : OperationsBundleProvider<PdfBoxRenderingContext, Document> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {}

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(TurnPageOperation { renderingContext, _ -> renderingContext.addPage() })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Document> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

}