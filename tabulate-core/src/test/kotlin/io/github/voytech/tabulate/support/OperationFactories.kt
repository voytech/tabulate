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
import io.github.voytech.tabulate.support.Spy.Companion.operationPriorities


class TestOutputBindingProvider: OutputBindingsProvider<TestRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<TestRenderingContext, *>> = listOf(
        TestOutputBinding(), OutputStreamTestOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> =
        format("spy")
}

class TestExportOperationsFactory : OperationsBundleProvider<TestRenderingContext, Table<Any>> {

    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(StartTableTestOperation(Spy.spy))
        operation(StartColumnTestOperation(Spy.spy))
        operation(StartRowTestOperation(Spy.spy))
        operation(RenderRowCellTestOperation(Spy.spy))
        operation(EndRowTestOperation<Table<Any>>(Spy.spy))
        operation(EndColumnTestOperation(Spy.spy))
        operation(EndTableTestOperation(Spy.spy))
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
        operation(CellTextStylesAttributeTestRenderOperation(Spy.spy),operationPriorities[TextStylesAttribute::class.java] ?: 1)
        operation(CellBordersAttributeTestRenderOperation(Spy.spy),operationPriorities[BordersAttribute::class.java] ?: 1)
        operation(CellBackgroundAttributeTestRenderOperation(Spy.spy),operationPriorities[BackgroundAttribute::class.java] ?: 1)
        operation(CellAlignmentAttributeTestRenderOperation(Spy.spy),operationPriorities[AlignmentAttribute::class.java] ?: 1)
        operation(ColumnWidthAttributeTestRenderOperation(Spy.spy),operationPriorities[WidthAttribute::class.java] ?: 1)
        operation(RowHeightAttributeTestRenderOperation(Spy.spy),operationPriorities[HeightAttribute::class.java] ?: 1)
        operation(TemplateFileAttributeTestRenderOperation(Spy.spy),operationPriorities[TemplateFileAttribute::class.java] ?: 1)
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