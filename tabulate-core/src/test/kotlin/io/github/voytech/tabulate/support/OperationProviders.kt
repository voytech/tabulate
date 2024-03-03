package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.operation.AttributeOperationsBuilder
import io.github.voytech.tabulate.core.operation.OperationsBuilder
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.result.OutputBinding
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.core.spi.OutputBindingsProvider


class TestOutputBindingProvider: OutputBindingsProvider<TestRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<TestRenderingContext, *>> = listOf(
        TestOutputBinding(), OutputStreamTestOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> =
        format("spy")
}


class AlternativeTestRenderingContext : RenderingContext

class AnotherTestExportOperationsFactory : OperationsBundleProvider<AlternativeTestRenderingContext, Table<Any>> {

    /**
     * atf - Alternative Test Format ;)
     */
    override fun getDocumentFormat(): DocumentFormat<AlternativeTestRenderingContext> =
        format("atf")

    override fun provideAttributeOperations(): AttributeOperationsBuilder<AlternativeTestRenderingContext>.() -> Unit = {
        // No attribute operations present at all.
    }

    override fun getRenderingContextClass(): Class<AlternativeTestRenderingContext> = reify()

    override fun provideExportOperations(): OperationsBuilder<AlternativeTestRenderingContext>.() -> Unit = {
        // No export operations present at all
    }

    override fun getModelClass(): Class<Table<Any>> = reify()

}