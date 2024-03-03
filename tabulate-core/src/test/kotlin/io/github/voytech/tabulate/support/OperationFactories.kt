package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.operation.AttributeOperationsBuilder
import io.github.voytech.tabulate.core.operation.OperationsBuilder
import io.github.voytech.tabulate.core.result.OutputBinding
import io.github.voytech.tabulate.core.spi.*
import io.github.voytech.tabulate.core.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.support.mock.Spy.Companion.operationPriorities
import io.github.voytech.tabulate.support.mock.components.*


class TestOutputBindingProvider: OutputBindingsProvider<TestRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<TestRenderingContext, *>> = listOf(
        TestOutputBinding(), OutputStreamTestOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> =
        format("spy")
}

class TestExportOperationsFactory : OperationsBundleProvider<TestRenderingContext, Table<Any>> {

    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(StartTableTestOperation())
        operation(StartColumnTestOperation())
        operation(StartRowTestOperation())
        operation(RenderRowCellTestOperation())
        operation(EndRowTestOperation())
        operation(EndColumnTestOperation())
        operation(EndTableTestOperation())
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
        operation(CellTextStylesAttributeTestRenderOperation(),operationPriorities[TextStylesAttribute::class.java] ?: 1)
        operation(CellBordersAttributeTestRenderOperation(),operationPriorities[BordersAttribute::class.java] ?: 1)
        operation(CellBackgroundAttributeTestRenderOperation(),operationPriorities[BackgroundAttribute::class.java] ?: 1)
        operation(CellAlignmentAttributeTestRenderOperation(),operationPriorities[AlignmentAttribute::class.java] ?: 1)
        operation(ColumnWidthAttributeTestRenderOperation(),operationPriorities[WidthAttribute::class.java] ?: 1)
        operation(RowHeightAttributeTestRenderOperation(),operationPriorities[HeightAttribute::class.java] ?: 1)
        operation(TemplateFileAttributeTestRenderOperation(),operationPriorities[TemplateFileAttribute::class.java] ?: 1)
    }

    override fun getRenderingContextClass(): Class<TestRenderingContext> = reify()

    override fun getModelClass(): Class<Table<Any>> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = format("spy")

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