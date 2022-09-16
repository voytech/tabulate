package io.github.voytech.tabulate.pdf.components.text

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.RenderTextOperation
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext


class PdfTextOperations : ExportOperationsProvider<PdfBoxRenderingContext, Text> {

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(RenderTextOperation { renderingContext, context ->
            with(renderingContext) {
                beginText()
                val box = renderingContext.boxLayout(context, context.getModelAttribute<BordersAttribute>())
                setTextPosition(box.innerX + xTextOffset, box.innerY + yTextOffset)
                showText(context.text)
                endText()
            }
        })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Text> = reify()

}