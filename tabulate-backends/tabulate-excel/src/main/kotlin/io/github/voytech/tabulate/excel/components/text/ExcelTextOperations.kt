package io.github.voytech.tabulate.excel.components.text

import io.github.voytech.tabulate.components.table.operation.getSheetName
import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextOperation
import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.*
import org.apache.poi.xssf.usermodel.XSSFTextBox


class ExcelTextOperations : OperationsBundleProvider<ApachePoiRenderingContext, Text> {

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(TextOperation { renderingContext, context ->
            with(renderingContext) {
                renderingContext.provideSheet(context.getSheetName()).let {
                    val clientAnchor = context.computeClientAnchor()
                    val textBox: XSSFTextBox = ensureDrawingPatriarch(it.sheetName).createTextbox(clientAnchor)
                    val simpleShapeWrapper = SimpleShapeWrapper(it.drawingPatriarch, textBox).bind(context)
                    simpleShapeWrapper.append(context.text)
                }
            }
        })
    }

    override fun provideMeasureOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(TextOperation { _, context -> })
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {
         operation(XSSFShapeAlignmentAttributeRenderOperation<TextRenderable>())
         operation(XSSFShapeTextStylesAttributeRenderOperation<TextRenderable>())
         operation(XSSFShapeBackgroundAttributeRenderOperation<TextRenderable>())
         operation(XSSFBorderAttributeRenderOperation<TextRenderable>())
    }

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> = DocumentFormat.format("xlsx", "poi")

    override fun getModelClass(): Class<Text> = reify()

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

}