package io.github.voytech.tabulate.pdf.components.table

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.components.table.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.layout.boundaries
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext
import org.apache.pdfbox.pdmodel.font.PDType1Font

class PdfTableOperations : ExportOperationsFactory<PdfBoxRenderingContext, Table<Any>>() {

    override fun provideExportOperations(): OperationsBuilder<PdfBoxRenderingContext, Table<Any>>.() -> Unit = {

        operation(StartTableOperation { _, _ -> })

        operation(StartColumnOperation { _, context ->
            context.getModelAttribute(ColumnWidthAttribute::class.java)?.px?.let {
                context.boundaries()?.width = Width(it.toFloat(),UnitsOfMeasure.PX)
            }
        })

        operation(StartRowOperation { _, context ->
            context.getModelAttribute(RowHeightAttribute::class.java)?.px?.let {
                context.boundaries()?.height = Height(it.toFloat(),UnitsOfMeasure.PX)
            }
        })

        operation(RenderRowCellOperation { renderingContext, context -> with(renderingContext) {
            renderingContext.getCurrentContentStream().let { content ->
                val height = renderingContext.getPage(0).mediaBox.height
                content.beginText()
                content.setFont( PDType1Font.HELVETICA_BOLD, 12F )
                content.newLineAtOffset(
                    (context.boundaries()?.absoluteX?.value!!),
                    (height - context.boundaries()?.absoluteY?.value!! - 20)
                )
                content.showText(context.rawValue.toString())
                content.endText()
            }
        }})
        operation(EndRowOperation { _, _ ->

        })
        operation(EndColumnOperation { _, _ ->

        })
        operation(EndTableOperation { _, _ -> })
    }

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")


    override fun getModelClass(): Class<Table<Any>> = reify()
}