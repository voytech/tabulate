package io.github.voytech.tabulate.pdf.components.text

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextOperation
import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.*


class PdfTextOperations : OperationsBundleProvider<PdfBoxRenderingContext, Text> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(BackgroundAttributeRenderOperation<TextRenderable>(), -3)
        operation(BordersAttributeRenderOperation<TextRenderable>(), -2)
        operation(TextStylesAttributeRenderOperation<TextRenderable>(), -1)
        operation(AlignmentAttributeRenderOperation<TextRenderable>(), -1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(TextOperation { renderingContext, context ->
            with(renderingContext) {
                beginText()
                val box = renderingContext.boxLayout(context, context.getModelAttribute<BordersAttribute>())
                setTextPosition(box.innerX + xTextOffset, box.innerY + yTextOffset + context.fontSize().descender())
                showText(context.text)
                endText()
            }
        })
    }

    override fun provideMeasureOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(TextOperation { _, context -> context.resolveTextBoundingBox() })
    }

    override fun getModelClass(): Class<Text> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

}