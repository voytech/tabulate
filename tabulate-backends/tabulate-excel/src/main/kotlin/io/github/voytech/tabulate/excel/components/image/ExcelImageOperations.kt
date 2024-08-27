package io.github.voytech.tabulate.excel.components.image

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageRenderableEntity
import io.github.voytech.tabulate.components.table.rendering.getSheetName
import io.github.voytech.tabulate.core.operation.VoidOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.excel.measureImage
import io.github.voytech.tabulate.loadImageAsByteArray
import org.apache.poi.ss.usermodel.Workbook

class ExcelImageOperations : OperationsBundleProvider<ApachePoiRenderingContext, Image> {

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(VoidOperation<ApachePoiRenderingContext,ImageRenderableEntity> { renderingContext, context ->
            with(renderingContext) {
                renderingContext.provideSheet(context.getSheetName()).let {
                    val picIndex = workbook().addPicture(context.filePath.loadImageAsByteArray(), Workbook.PICTURE_TYPE_PNG)
                    val clientAnchor = context.createApachePoiSpreadsheetAnchor()
                    val picture = ensureDrawingPatriarch(context.getSheetName()).createPicture(clientAnchor,picIndex)
                    picture.bind(context)
                }
            }
        })
    }

    override fun provideMeasureOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(VoidOperation<ApachePoiRenderingContext,ImageRenderableEntity> { renderingContext, context ->
            with(renderingContext) {
                context.checkSizeDeclarations()
                context.measureImage()
                context.createSpreadsheetAnchor()
                context.applySpreadsheetAnchor()
            }
        })
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {

    }

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> = DocumentFormat.format("xlsx", "poi")

    override fun getModelClass(): Class<Image> = reify()

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

}