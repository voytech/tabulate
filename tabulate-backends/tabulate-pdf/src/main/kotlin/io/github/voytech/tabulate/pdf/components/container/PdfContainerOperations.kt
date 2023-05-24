package io.github.voytech.tabulate.pdf.components.container

import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.components.container.opration.ContainerOperation
import io.github.voytech.tabulate.components.container.opration.ContainerRenderable
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.pdf.BackgroundAttributeRenderOperation
import io.github.voytech.tabulate.pdf.BordersAttributeRenderOperation
import io.github.voytech.tabulate.pdf.PdfBoxRenderingContext


class PdfContainerOperations : OperationsBundleProvider<PdfBoxRenderingContext, Container> {

    override fun provideAttributeOperations(): BuildAttributeOperations<PdfBoxRenderingContext> = {
        operation(BackgroundAttributeRenderOperation<ContainerRenderable>(), -2)
        operation(BordersAttributeRenderOperation<ContainerRenderable>(), -1)
    }

    override fun provideExportOperations(): BuildOperations<PdfBoxRenderingContext> = {
        operation(ContainerOperation { _, _ -> })
    }

    override fun getModelClass(): Class<Container> = reify()

    override fun getRenderingContextClass(): Class<PdfBoxRenderingContext> = reify()

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> = DocumentFormat.format("pdf", "pdfbox")

}