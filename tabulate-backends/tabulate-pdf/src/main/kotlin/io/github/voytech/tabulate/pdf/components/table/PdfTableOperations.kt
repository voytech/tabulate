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
        operation(BackgroundAttributeRenderOperation<CellRenderableEntity>(), -3)
        operation(BordersAttributeRenderOperation<CellRenderableEntity>(), 1)
        operation(BordersAttributeRenderOperation<RowEndRenderableEntity<Table<*>>>(), -1)
        operation(BordersAttributeRenderOperation<TableStartRenderableEntity>(),-1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(StartTableOperation { renderingContext, _ -> renderingContext.createPageIfMissing()})
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
        operation(StartTableOperation { renderingContext, _ -> renderingContext.createPageIfMissing()}) // TODO for standalone rendering !!!
        operation(StartColumnOperation { _, _ -> }) // TODO fix table template to make it not requiring empty operations.
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

private fun CellRenderableEntity.asPdfBoxTextElement(): PdfBoxText = PdfBoxText(
    value.toString(), requireNotNull(boundingBox()), textMeasures(), paddings(),
    getModelAttribute<TextStylesAttribute>(), getModelAttribute<AlignmentAttribute>()
)

private fun CellRenderableEntity.asPdfBoxImageElement(image: PDImageXObject): PdfBoxImage = PdfBoxImage(
    image, requireNotNull(boundingBox()), paddings(),
    getModelAttribute<AlignmentAttribute>()
)
