package io.github.voytech.tabulate.pdf.components.table

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext


class PdfTableOperations : OperationsBundleProvider<PdfBoxRenderingContext, Table<Any>> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(CellBackgroundAttributeRenderOperation(), -3)
        operation(CellBordersAttributeRenderOperation(), -2)
        operation(RowBordersAttributeRenderOperation<Table<Any>>(), -2)
        operation(CellTextStylesAttributeRenderOperation(), -1)
        operation(CellAlignmentAttributeRenderOperation(), -1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {

        operation(StartTableOperation { _, _ -> })

        operation(StartColumnOperation { _, _ -> })

        operation(StartRowOperation { _, _ -> })

        operation(RenderRowCellOperation { renderingContext, context ->
            with(renderingContext) {
                beginText()
                val box = renderingContext.boxLayout(context, context.getModelAttribute<BordersAttribute>()) //TODO boxModel should be a part of boundingRectangle comming from core
                setTextPosition(box.innerX + xTextOffset, box.innerY + yTextOffset)
                showText(context.rawValue.toString())
                endText()
            }
        })
        operation(EndRowOperation<PdfBoxRenderingContext, Table<Any>> { _, _ ->

        })
        operation(EndColumnOperation { _, _ ->

        })
        operation(EndTableOperation { _, _ -> })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Table<Any>> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

}