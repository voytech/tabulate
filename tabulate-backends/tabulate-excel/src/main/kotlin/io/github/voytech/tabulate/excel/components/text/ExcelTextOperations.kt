package io.github.voytech.tabulate.excel.components.text

import io.github.voytech.tabulate.components.table.operation.getSheetName
import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextOperation
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.ShapeTypes
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFTextBox

class ExcelTextOperations : OperationsBundleProvider<ApachePoiRenderingContext, Text> {

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(TextOperation { renderingContext, context ->
            with(renderingContext) {
                val anchor = context.createSpreadSheetAnchor()
                renderingContext.provideSheet(context.getSheetName()).let {
                    val clientAnchor = createClientAnchor() as XSSFClientAnchor
                    clientAnchor.setCol1(anchor.leftTopColumn)
                    clientAnchor.row1 = anchor.leftTopRow
                    clientAnchor.setCol2(anchor.rightBottomColumn)
                    clientAnchor.row2 = anchor.rightBottomRow
                    it.createDrawingPatriarch()
                    val textBox: XSSFTextBox = it.drawingPatriarch.createTextbox(clientAnchor)
                    val color = Colors.BLUE
                    textBox.isNoFill = false
                    textBox.setFillColor(color.r,color.g,color.b)
                    textBox.shapeType = ShapeTypes.RECT
                    textBox.setLineStyleColor(0, 0, 0)
                    textBox.setLineWidth(2.0);
                    textBox.text = context.text
                    textBox.verticalAlignment = VerticalAlignment.CENTER

                }
            }
        })
    }

    override fun provideMeasureOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(TextOperation { _, context -> })
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {
        /*
         operation(BackgroundAttributeRenderOperation<TextRenderable>(), -3)
         operation(BordersAttributeRenderOperation<TextRenderable>(), -2)
         operation(TextStylesAttributeRenderOperation<TextRenderable>(), -1)
         operation(AlignmentAttributeRenderOperation<TextRenderable>(), -1)
         */
    }

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> = DocumentFormat.format("xlsx", "poi")

    override fun getModelClass(): Class<Text> = reify()

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

}