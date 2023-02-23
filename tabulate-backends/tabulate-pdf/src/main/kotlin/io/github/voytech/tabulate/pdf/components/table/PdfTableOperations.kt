package io.github.voytech.tabulate.pdf.components.table

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.*

class PdfTableOperations : OperationsBundleProvider<PdfBoxRenderingContext, Table<Any>> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(BackgroundAttributeRenderOperation<CellContext>(), -3)
        operation(BordersAttributeRenderOperation<CellContext>(), -2)
        operation(BordersAttributeRenderOperation<RowEnd<Table<*>>>(), -2)
        operation(TextStylesAttributeRenderOperation<CellContext>(), -1)
        operation(AlignmentAttributeRenderOperation<CellContext>(), -1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {

        operation(StartTableOperation { _, _ -> })

        operation(StartColumnOperation { _, _ -> })

        operation(StartRowOperation { _, _ -> })

        // TODO support typeHits
        operation(RenderRowCellOperation { renderingContext, context ->
            with(renderingContext) {
                context.getTypeHint().let {
                    if (it?.type == DefaultTypeHints.IMAGE_URI) {
                        context.renderImageFromURI(context.value.toString())
                    } else {
                        beginText()
                        // TODO boxModel should be a part of boundingRectangle from library core
                        val box = renderingContext.boxLayout(context, context.getModelAttribute<BordersAttribute>())
                        setTextPosition(
                            box.innerX + xTextOffset,
                            box.innerY + yTextOffset + context.fontSize().descender()
                        )
                        showText(context.value.toString())
                        endText()
                    }
                }
            }
        })
        operation(EndRowOperation<PdfBoxRenderingContext, Table<Any>> { _, _ ->

        })
        operation(EndColumnOperation { _, _ ->

        })
        operation(EndTableOperation { _, _ -> })
    }

    override fun provideMeasureOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(StartTableOperation { _, _ -> }) // TODO fix table template to make it not requiring empty operations.
        operation(StartColumnOperation { _, _ -> })
        operation(StartRowOperation { _, _ -> })
        operation(RenderRowCellOperation { renderingContext, context ->
            with(renderingContext) {
                context.getTypeHint().let {
                    if (it?.type == DefaultTypeHints.IMAGE_URI) {
                        context.resolveUriImageBoundingBox(context.value.toString())
                    } else context.resolveTextBoundingBox()
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