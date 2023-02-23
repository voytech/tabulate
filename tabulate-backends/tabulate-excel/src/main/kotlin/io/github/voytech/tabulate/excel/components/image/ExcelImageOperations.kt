package io.github.voytech.tabulate.excel.components.image

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext

class ExcelImageOperations : OperationsBundleProvider<ApachePoiRenderingContext, Image> {

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext>  = {

    }

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {

    }

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> =  DocumentFormat.format("pdf", "pdfbox")

    override fun getModelClass(): Class<Image> = reify()

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

}