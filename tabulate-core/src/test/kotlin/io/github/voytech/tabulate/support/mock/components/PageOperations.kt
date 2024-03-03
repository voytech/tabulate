package io.github.voytech.tabulate.support.mock.components

import io.github.voytech.tabulate.components.commons.operation.NewPage
import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.ExportOperationsProvider
import io.github.voytech.tabulate.support.TestRenderingContext
import io.github.voytech.tabulate.support.mock.MockRenderOperation

class TestPageExportOperationsFactory: ExportOperationsProvider<TestRenderingContext, Page> {
    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(NewPageOperation())
    }

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = DocumentFormat.format("spy")

    override fun getModelClass(): Class<Page> = reify()
}

class NewPageOperation : MockRenderOperation<NewPage>(NewPage::class.java)
