package io.github.voytech.tabulate.pdf.components.image

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.components.image.operation.RenderImageOperation
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.layout.boundingBox
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
        operation(RenderImageOperation { renderingContext, context ->
            with(renderingContext) {
                val image = loadImage(context.filePath)
                context.boundingBox()?.let {
                    it.height = it.height ?: Height(image.height.toFloat(),UnitsOfMeasure.PT)
                    it.width = it.width ?: Width(image.width.toFloat(),UnitsOfMeasure.PT)
                }
                val box = renderingContext.boxLayout(context, context.getModelAttribute<BordersAttribute>())
                image.showImage(box.innerX,box.innerY,box.inner.width?.value,box.inner.height?.value)
            }
        })
    }

    override fun getModelClass(): Class<Image> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

}