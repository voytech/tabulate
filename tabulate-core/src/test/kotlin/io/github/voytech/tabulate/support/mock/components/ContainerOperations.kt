package io.github.voytech.tabulate.support.mock.components

import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.components.container.opration.ContainerRenderable
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

class TestContainerExportOperationsFactory : OperationsBundleProvider<TestRenderingContext, Container> {

    override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
        operation(MockAttributeRenderOperation<ContainerRenderable,BackgroundAttribute>(), -2)
        operation(MockAttributeRenderOperation<ContainerRenderable,BordersAttribute>(), -1)
    }

    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(ContainerOperation())
    }

    override fun getRenderingContextClass(): Class<TestRenderingContext> = reify()

    override fun getModelClass(): Class<Container> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = DocumentFormat.format("spy")

}

class ContainerOperation : MockRenderOperation<ContainerRenderable>(ContainerRenderable::class.java)
