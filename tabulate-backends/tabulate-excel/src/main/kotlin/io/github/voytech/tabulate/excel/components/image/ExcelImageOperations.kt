package io.github.voytech.tabulate.excel.components.image

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageOperation
import io.github.voytech.tabulate.components.table.operation.getSheetName
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.loadImageAsByteArray
import org.apache.poi.ss.usermodel.Workbook

class ExcelImageOperations : OperationsBundleProvider<ApachePoiRenderingContext, Image> {

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext>  = {
        operation(ImageOperation { renderingContext, context ->
            with(renderingContext) {
                renderingContext.provideSheet(context.getSheetName()).let {
                    val picIndex = workbook().addPicture(context.filePath.loadImageAsByteArray(), Workbook.PICTURE_TYPE_PNG)
                    val clientAnchor = context.computeClientAnchor()
                    val picture = ensureDrawingPatriarch(context.getSheetName()).createPicture(clientAnchor,picIndex)
                    picture.bind(context)
                }
            }
        })
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {

    }

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> = DocumentFormat.format("xlsx", "poi")

    override fun getModelClass(): Class<Image> = reify()

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

}