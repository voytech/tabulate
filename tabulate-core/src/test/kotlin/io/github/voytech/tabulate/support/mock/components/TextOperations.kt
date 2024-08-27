package io.github.voytech.tabulate.support.mock.components

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextRenderableEntity
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

class TestTextExportOperationsFactory : OperationsBundleProvider<TestRenderingContext, Text> {

    override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
        operation(MockAttributeRenderOperation<TextRenderableEntity, BackgroundAttribute>(), -3)
        operation(MockAttributeRenderOperation<TextRenderableEntity, BordersAttribute>(), -2)
    }

    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(TextOperation(false))
    }

    override fun provideMeasureOperations(): BuildOperations<TestRenderingContext> = {
        operation(TextOperation(true))
    }

    override fun getModelClass(): Class<Text> = reify()

    override fun getRenderingContextClass(): Class<TestRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = DocumentFormat.format("spy")

}

class TextOperation(isMeasuring: Boolean): MockRenderOperation<TextRenderableEntity>(TextRenderableEntity::class.java,isMeasuring)