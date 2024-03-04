package io.github.voytech.tabulate.support.mock.components

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.support.TestRenderingContext
import io.github.voytech.tabulate.support.mock.MockAttributeRenderOperation
import io.github.voytech.tabulate.support.mock.MockRenderOperation

class TestImageExportOperationsFactory : OperationsBundleProvider<TestRenderingContext, Image> {

    override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
        operation(MockAttributeRenderOperation<ImageRenderable, BackgroundAttribute>(), -3)
        operation(MockAttributeRenderOperation<ImageRenderable, BordersAttribute>(), -2)
    }

    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(ImageOperation(false))
    }

    override fun provideMeasureOperations(): BuildOperations<TestRenderingContext> = {
        operation(ImageOperation(true))
    }

    override fun getModelClass(): Class<Image> = reify()

    override fun getRenderingContextClass(): Class<TestRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = DocumentFormat.format("spy")

}

class ImageOperation(isMeasuring: Boolean): MockRenderOperation<ImageRenderable>(ImageRenderable::class.java,isMeasuring)