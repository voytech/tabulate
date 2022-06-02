package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider


class TestOutputBindingProvider: OutputBindingsProvider<TestRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<TestRenderingContext, *>> = listOf(
        TestOutputBinding(), OutputStreamTestOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> =
        format("spy")
}

class TestExportOperationsFactory : ExportOperationsFactory<TestRenderingContext, Table<Any>>() {

    override fun provideExportOperations(): OperationsBuilder<TestRenderingContext, Table<Any>>.() -> Unit = {
        operation(StartTableTestOperation(Spy.spy))
        operation(StartColumnTestOperation(Spy.spy))
        operation(StartRowTestOperation(Spy.spy))
        operation(RenderRowCellTestOperation(Spy.spy))
        operation(EndRowTestOperation(Spy.spy))
        operation(EndColumnTestOperation(Spy.spy))
        operation(EndTableTestOperation(Spy.spy))
    }

    override fun provideAttributeOperations() = setOf(
        CellTextStylesAttributeTestRenderOperation(Spy.spy),
        CellBordersAttributeTestRenderOperation(Spy.spy),
        CellBackgroundAttributeTestRenderOperation(Spy.spy),
        CellAlignmentAttributeTestRenderOperation(Spy.spy),
        ColumnWidthAttributeTestRenderOperation(Spy.spy),
        RowHeightAttributeTestRenderOperation(Spy.spy),
        TemplateFileAttributeTestRenderOperation(Spy.spy)
    )

    override fun getModelClass(): Class<Table<Any>> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> =
        format("spy")

}

class AlternativeTestRenderingContext : RenderingContext

class AnotherTestExportOperationsFactory : ExportOperationsFactory<AlternativeTestRenderingContext,Table<Any>>() {

    /**
     * atf - Alternative Test Format ;)
     */
    override fun getDocumentFormat():  DocumentFormat<AlternativeTestRenderingContext> =
        format("atf")

    override fun provideExportOperations(): OperationsBuilder<AlternativeTestRenderingContext,Table<Any>>.() -> Unit = {
        // Empty all
    }

    override fun getModelClass(): Class<Table<Any>> = reify()

}