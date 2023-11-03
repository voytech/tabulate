package io.github.voytech.tabulate.pdf.components.image

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageOperation
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.operation.boundingBox
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.*
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject


class PdfImageOperations : OperationsBundleProvider<PdfBoxRenderingContext, Image> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(BackgroundAttributeRenderOperation<ImageRenderable>(), -2)
        operation(BordersAttributeRenderOperation<ImageRenderable>(), -1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(ImageOperation { renderingContext, context ->
            val image = renderingContext.loadImage(context.filePath)
            context.asPdfBoxElement(image).render(renderingContext)
        })
    }

    override fun provideMeasureOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(ImageOperation { renderingContext, context ->
            val image = renderingContext.loadImage(context.filePath)
            context.asPdfBoxElement(image).measure(renderingContext)
        })
    }

    override fun getModelClass(): Class<Image> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

}

private fun ImageRenderable.asPdfBoxElement(image: PDImageXObject): PdfBoxImage = PdfBoxImage(
    image, requireNotNull(boundingBox()), paddings(),
    getModelAttribute<AlignmentAttribute>()
)
