package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributeOperationsBuilder
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.*
import io.github.voytech.tabulate.core.template.spi.DocumentFormat.Companion.format


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
        operation(EndRowTestOperation(Spy.spy))
        operation(EndColumnTestOperation(Spy.spy))
        operation(EndTableTestOperation(Spy.spy))
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
        operation(CellTextStylesAttributeTestRenderOperation(Spy.spy))
        operation(CellBordersAttributeTestRenderOperation(Spy.spy))
        operation(CellBackgroundAttributeTestRenderOperation(Spy.spy))
        operation(CellAlignmentAttributeTestRenderOperation(Spy.spy))
        operation(ColumnWidthAttributeTestRenderOperation(Spy.spy))
        operation(RowHeightAttributeTestRenderOperation(Spy.spy))
        operation(TemplateFileAttributeTestRenderOperation(Spy.spy))
    }

    override fun getRenderingContextClass(): Class<TestRenderingContext> = reify()

    override fun getModelClass(): Class<Table<Any>> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = format("spy")

}

class AlternativeTestRenderingContext : RenderingContext

class AnotherTestExportOperationsFactory : OperationsBundleProvider<AlternativeTestRenderingContext,Table<Any>>  {

    /**
     * atf - Alternative Test Format ;)
     */
    override fun getDocumentFormat():  DocumentFormat<AlternativeTestRenderingContext> =
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