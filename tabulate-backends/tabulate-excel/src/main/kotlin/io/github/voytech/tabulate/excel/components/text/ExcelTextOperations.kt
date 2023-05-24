package io.github.voytech.tabulate.excel.components.text

import io.github.voytech.tabulate.components.table.operation.getSheetName
import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextOperation
import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.operation.VoidOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.*
import org.apache.poi.xssf.usermodel.XSSFTextBox


class ExcelTextOperations : OperationsBundleProvider<ApachePoiRenderingContext, Text> {

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(VoidOperation<ApachePoiRenderingContext,TextRenderable> { renderingContext, context ->
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
        operation(VoidOperation<ApachePoiRenderingContext,TextRenderable> { _, _ -> })
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