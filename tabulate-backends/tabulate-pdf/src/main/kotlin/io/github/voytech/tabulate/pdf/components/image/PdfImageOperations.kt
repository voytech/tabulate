package io.github.voytech.tabulate.pdf.components.image

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageOperation
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.boundingBox
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.BackgroundAttributeRenderOperation
import io.github.voytech.tabulate.pdf.BordersAttributeRenderOperation
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext


class PdfImageOperations : OperationsBundleProvider<PdfBoxRenderingContext, Image> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(BackgroundAttributeRenderOperation<ImageRenderable>(), -2)
        operation(BordersAttributeRenderOperation<ImageRenderable>(), -1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(ImageOperation { renderingContext, context ->
            with(renderingContext) {
                with(renderingContext.boxLayout(context, context.getModelAttribute<BordersAttribute>())) {
                    loadImage(context.filePath).showImage(
                        innerX,
                        innerY,
                        inner.width?.value,
                        inner.height?.value
                    )
                }
            }
        })
    }

    override fun provideMeasureOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(ImageOperation { renderingContext, context ->
            context.boundingBox()?.let { bbox ->
                if (bbox.height == null || bbox.width == null) {
                    val image = renderingContext.loadImage(context.filePath)
                    bbox.apply {
                        height = height ?: Height(image.height.toFloat(), UnitsOfMeasure.PT)
                        width = width ?: Width(image.width.toFloat(), UnitsOfMeasure.PT)
                    }
                }
            } ?: error("Image renderable context requires bbox in order to render properly.")
        })
    }

    override fun getModelClass(): Class<Image> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

}