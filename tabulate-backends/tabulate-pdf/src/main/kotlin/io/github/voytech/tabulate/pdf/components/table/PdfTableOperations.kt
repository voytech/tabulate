package io.github.voytech.tabulate.pdf.components.table

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.rendering.*
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.operation.Nothing
import io.github.voytech.tabulate.core.operation.asResult
import io.github.voytech.tabulate.core.operation.boundingBox
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.*
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

class PdfTableOperations : OperationsBundleProvider<PdfBoxRenderingContext, Table<Any>> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(BackgroundAttributeRenderOperation<CellRenderable>(), -3)
        operation(BordersAttributeRenderOperation<CellRenderable>(), -2)
        operation(BordersAttributeRenderOperation<RowEndRenderable<Table<*>>>(), -2)
        //operation(TextStylesAttributeRenderOperation<CellRenderable>(), -1) //TODO drop this attribute, cannot be handled by separate render.
        operation(BordersAttributeRenderOperation<TableStartRenderable>())
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(StartTableOperation { _, _ -> })
        operation(StartColumnOperation { _, _ -> })
        operation(StartRowOperation { _, _ -> })
        // TODO support typeHits
        operation(RenderRowCellOperation { renderingContext, context ->
            context.getTypeHint().let {
                if (it?.type == DefaultTypeHints.IMAGE_URI) {
                    val image = renderingContext.loadImage(context.value.toString())
                    context.asPdfBoxImageElement(image).render(renderingContext)
                } else {
                    context.asPdfBoxTextElement().render(renderingContext)
                }
            }
        })
        operation(EndRowOperation<PdfBoxRenderingContext, Table<Any>> { _, _ -> Nothing.asResult() })
        operation(EndColumnOperation { _, _ -> })
        operation(EndTableOperation { _, _ -> })
    }

    override fun provideMeasureOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(StartTableOperation { _, _ -> }) // TODO fix table template to make it not requiring empty operations.
        operation(StartColumnOperation { _, _ -> })
        operation(StartRowOperation { _, _ -> })
        operation(RenderRowCellOperation { renderingContext, context ->
            context.getTypeHint().let {
                if (it?.type == DefaultTypeHints.IMAGE_URI) {
                    val image = renderingContext.loadImage(context.value.toString())
                    context.asPdfBoxImageElement(image).measure(renderingContext)
                } else { // suppose this is always text for now
                    context.asPdfBoxTextElement().measure(renderingContext)
                }
            }
        })
        operation(EndRowOperation<PdfBoxRenderingContext, Table<Any>> { _, _ -> })
        operation(EndColumnOperation { _, _ -> })
        operation(EndTableOperation { _, _ -> })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Table<Any>> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

}

private fun CellRenderable.asPdfBoxTextElement(): PdfBoxText = PdfBoxText(
    value.toString(), requireNotNull(boundingBox()), textMeasures(), paddings(),
    getModelAttribute<TextStylesAttribute>(), getModelAttribute<AlignmentAttribute>()
)

private fun CellRenderable.asPdfBoxImageElement(image: PDImageXObject): PdfBoxImage = PdfBoxImage(
    image, requireNotNull(boundingBox()), paddings(),
    getModelAttribute<AlignmentAttribute>()
)
