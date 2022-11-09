package io.github.voytech.tabulate.pdf.components.image

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.components.image.operation.RenderImageOperation
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.boundingBox
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.BackgroundAttributeRenderOperation
import io.github.voytech.tabulate.pdf.BordersAttributeRenderOperation
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext


class PdfImageOperations : OperationsBundleProvider<PdfBoxRenderingContext, Image> {

    private val requireBoundingBox: RenderImageOperation<PdfBoxRenderingContext> =
        RenderImageOperation { renderingContext, context ->
            context.boundingBox()?.let { bbox ->
                if (bbox.height == null || bbox.width == null) {
                    val image = renderingContext.loadImage(context.filePath)
                    bbox.apply {
                        height = height ?: Height(image.height.toFloat(), UnitsOfMeasure.PT)
                        width = width ?: Width(image.width.toFloat(), UnitsOfMeasure.PT)
                    }
                }
            } ?: error("Image renderable context requires bbox in order to render properly.")
        }

    inner class RequireBoundingBox<A : Attribute<A>>(
        val delegate: AttributeOperation<PdfBoxRenderingContext, A, ImageRenderable>,
    ) : AttributeOperation<PdfBoxRenderingContext, A, ImageRenderable> {
        override fun invoke(renderingContext: PdfBoxRenderingContext, context: ImageRenderable, attribute: A) {
            requireBoundingBox(renderingContext, context)
            delegate(renderingContext, context, attribute)
        }
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(RequireBoundingBox(BackgroundAttributeRenderOperation()), -2)
        operation(RequireBoundingBox(BordersAttributeRenderOperation()), -1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(RenderImageOperation { renderingContext, context ->
            with(renderingContext) {
                requireBoundingBox(renderingContext, context)
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

    override fun getModelClass(): Class<Image> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

}