package io.github.voytech.tabulate.pdf.components.text

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextOperation
import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.operation.boundingBox
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.*

class PdfTextOperations : OperationsBundleProvider<PdfBoxRenderingContext, Text> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(BackgroundAttributeRenderOperation<TextRenderable>(), -3)
        operation(BordersAttributeRenderOperation<TextRenderable>(), 1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(TextOperation { rendering, context ->
            context.asPdfBoxElement().render(rendering)
        })
    }

    override fun provideMeasureOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(TextOperation { rendering, context ->
            context.asPdfBoxElement().measure(rendering)
        })
    }

    override fun getModelClass(): Class<Text> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

}

private fun TextRenderable.asPdfBoxElement(): PdfBoxText {
    return PdfBoxText(
        text, requireNotNull(boundingBox()), textMeasures(), paddings(),
        getModelAttribute<TextStylesAttribute>(),
        getModelAttribute<AlignmentAttribute>()
    )
}