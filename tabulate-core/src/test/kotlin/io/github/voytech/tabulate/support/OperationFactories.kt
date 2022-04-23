package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.operation.factories.AttributeOperationsFactory
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider


class TestAttributeOperationsFactory : AttributeOperationsFactory<TestRenderingContext, Table<*>> {

    override fun createAttributeOperations(): Set<AttributeOperation<TestRenderingContext, Table<*>, *, *, *>> = setOf(
        CellTextStylesAttributeTestRenderOperation(Spy.spy),
        CellBordersAttributeTestRenderOperation(Spy.spy),
        CellBackgroundAttributeTestRenderOperation(Spy.spy),
        CellAlignmentAttributeTestRenderOperation(Spy.spy),
        ColumnWidthAttributeTestRenderOperation(Spy.spy),
        RowHeightAttributeTestRenderOperation(Spy.spy),
        TemplateFileAttributeTestRenderOperation(Spy.spy)
    )

    override fun getRenderingContextClass(): Class<TestRenderingContext> = reify()

    override fun getRootModelClass(): Class<Table<*>> = reify()

}

class TestOutputBindingProvider: OutputBindingsProvider<TestRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<TestRenderingContext, *>> = listOf(
        TestOutputBinding(), OutputStreamTestOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> =
        format("spy")
}

class TestExportOperationsFactory : ExportOperationsFactory<TestRenderingContext, Table<*>>() {

    override fun provideExportOperations(): OperationsBuilder<TestRenderingContext, Table<*>>.() -> Unit = {
        operation(OpenTableTestOperation(Spy.spy))
        operation(OpenColumnTestOperation(Spy.spy))
        operation(OpenRowTestOperation(Spy.spy))
        operation(RenderRowCellTestOperation(Spy.spy))
        operation(CloseRowTestOperation(Spy.spy))
        operation(CloseColumnTestOperation(Spy.spy))
        operation(CloseTableTestOperation(Spy.spy))
    }

    override fun getAttributeOperationsFactory(): AttributeOperationsFactory<TestRenderingContext, Table<*>> =
        TestAttributeOperationsFactory()

    override fun getAggregateModelClass(): Class<Table<*>> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> =
        format("spy")

}

class AlternativeTestRenderingContext : RenderingContext

class AnotherTestExportOperationsFactory : ExportOperationsFactory<AlternativeTestRenderingContext,Table<*>>() {

    /**
     * atf - Alternative Test Format ;)
     */
    override fun getDocumentFormat():  DocumentFormat<AlternativeTestRenderingContext> =
        format("atf")

    override fun provideExportOperations(): OperationsBuilder<AlternativeTestRenderingContext,Table<*>>.() -> Unit = {
        // Empty all
    }

    override fun getAggregateModelClass(): Class<Table<*>> = reify()

}