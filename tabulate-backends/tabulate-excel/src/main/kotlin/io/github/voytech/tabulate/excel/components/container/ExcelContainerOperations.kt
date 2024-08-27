package io.github.voytech.tabulate.excel.components.container

import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.components.container.operation.ContainerOperation
import io.github.voytech.tabulate.components.container.operation.ContainerRenderableEntity
import io.github.voytech.tabulate.components.table.rendering.getSheetName
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.excel.SimpleShapeWrapper
import io.github.voytech.tabulate.excel.XSSFBorderAttributeRenderOperation
import io.github.voytech.tabulate.excel.XSSFShapeBackgroundAttributeRenderOperation
import org.apache.poi.xssf.usermodel.XSSFSimpleShape

class ExcelContainerOperations : OperationsBundleProvider<ApachePoiRenderingContext, Container> {

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {
        operation(XSSFShapeBackgroundAttributeRenderOperation<ContainerRenderableEntity>())
        operation(XSSFBorderAttributeRenderOperation<ContainerRenderableEntity>())
    }

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(ContainerOperation {renderingContext, context ->
            with(renderingContext) {
                renderingContext.provideSheet(context.getSheetName()).let { sheet ->
                    val clientAnchor = context.createApachePoiSpreadsheetAnchor()
                    val shape: XSSFSimpleShape = ensureDrawingPatriarch(sheet.sheetName).createSimpleShape(clientAnchor)
                    SimpleShapeWrapper(sheet.drawingPatriarch, shape).bind(context)
                    context.applySpreadsheetAnchor()
                }
            }
        })
    }

    override fun getModelClass(): Class<Container> = reify()

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> = DocumentFormat.format("xlsx", "poi")

}