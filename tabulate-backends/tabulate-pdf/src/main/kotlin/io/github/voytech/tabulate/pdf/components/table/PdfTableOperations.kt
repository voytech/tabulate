package io.github.voytech.tabulate.pdf.components.table

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.layout.boundaries
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.awt.Color

class PdfTableOperations : ExportOperationsProvider<PdfBoxRenderingContext, Table<Any>> {

    private fun font(context: CellContext): PDFont =
        context.getModelAttribute<CellTextStylesAttribute>()?.let {
            when (it.fontFamily) {
                "Times-Roman" -> PDType1Font.TIMES_ROMAN
                else -> PDType1Font.HELVETICA
            }
        } ?: PDType1Font.HELVETICA

    private fun fontSize(context: CellContext): Float =
        context.getModelAttribute<CellTextStylesAttribute>()?.let {
            it.fontSize?.toFloat()
        } ?: 10F

    private fun fontColor(context: CellContext): Color =
        context.getModelAttribute<CellTextStylesAttribute>()?.let { style ->
            style.fontColor?.let { Color(it.r,it.g,it.b) }
        } ?: Color.BLACK

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {

        operation(StartTableOperation { _, _ -> })

        operation(StartColumnOperation { _, _ -> })

        operation(StartRowOperation { _, _ -> })

        operation(RenderRowCellOperation { renderingContext, context ->
            with(renderingContext) {
                getCurrentContentStream().let { content ->
                    val height = getCurrentPage().mediaBox.height
                    content.beginText()
                    content.setFont(font(context), fontSize(context))
                    content.setNonStrokingColor(fontColor(context))
                    content.newLineAtOffset(
                        (context.boundaries()?.absoluteX?.value!!),
                        (height - context.boundaries()?.absoluteY?.value!! - 20)
                    )
                    content.showText(context.rawValue.toString())
                    content.endText()
                }
            }
        })
        operation(EndRowOperation<PdfBoxRenderingContext,Table<Any>> { _, _ ->

        })
        operation(EndColumnOperation { _, _ ->

        })
        operation(EndTableOperation { _, _ -> })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")


    override fun getModelClass(): Class<Table<Any>> = reify()
}