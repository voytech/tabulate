package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.AttributeOperation
import io.github.voytech.tabulate.template.operations.AttributeOperationsFactory
import io.github.voytech.tabulate.template.operations.ExportOperationsFactory
import io.github.voytech.tabulate.template.operations.OperationsBuilder
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.spi.TabulationFormat
import io.github.voytech.tabulate.template.spi.TabulationFormat.Companion.format

class TestAttributeOperationsFactory : AttributeOperationsFactory<TestRenderingContext> {
    override fun createCellAttributeRenderOperations(): Set<AttributeOperation<TestRenderingContext,CellAttribute<*>,*, *>> =
        setOf(
            CellTextStylesAttributeTestRenderOperation(Spy.spy),
            CellBordersAttributeTestRenderOperation(Spy.spy),
            CellBackgroundAttributeTestRenderOperation(Spy.spy),
            CellAlignmentAttributeTestRenderOperation(Spy.spy)
        )

    override fun createColumnAttributeRenderOperations(): Set<AttributeOperation<TestRenderingContext,ColumnAttribute<*>,*, *>> =
        setOf(ColumnWidthAttributeTestRenderOperation(Spy.spy))

    override fun createRowAttributeRenderOperations(): Set<AttributeOperation<TestRenderingContext,RowAttribute<*>,*, *>> =
        setOf(RowHeightAttributeTestRenderOperation(Spy.spy))

    override fun createTableAttributeRenderOperations(): Set<AttributeOperation<TestRenderingContext,TableAttribute<*>,*, *>> =
        setOf(TemplateFileAttributeTestRenderOperation(Spy.spy))

}

class TestExportOperationsFactory : ExportOperationsFactory<TestRenderingContext>() {

    override fun provideExportOperations(): OperationsBuilder<TestRenderingContext>.() -> Unit = {
        openTable = OpenTableTestOperation(Spy.spy)
        openColumn = OpenColumnTestOperation(Spy.spy)
        openRow = OpenRowTestOperation(Spy.spy)
        renderRowCell = RenderRowCellTestOperation(Spy.spy)
        closeRow = CloseRowTestOperation(Spy.spy)
        closeColumn = CloseColumnTestOperation(Spy.spy)
        closeTable = CloseTableTestOperation(Spy.spy)
    }

    override fun createOutputBindings(): List<OutputBinding<TestRenderingContext, *>> = listOf(
        TestOutputBinding(), OutputStreamTestOutputBinding()
    )

    override fun createRenderingContext(): TestRenderingContext = TestRenderingContext().also {
        CURRENT_RENDERING_CONTEXT_INSTANCE = it
    }

    override fun getTabulationFormat(): TabulationFormat<TestRenderingContext> =
        format("spy", TestRenderingContext::class.java)

    override fun getAttributeOperationsFactory(): AttributeOperationsFactory<TestRenderingContext> =
        TestAttributeOperationsFactory()

    companion object {
        @JvmStatic
        var CURRENT_RENDERING_CONTEXT_INSTANCE: TestRenderingContext? = null
    }

}

class AlternativeTestRenderingContext : RenderingContext

class AnotherTestExportOperationsFactory : ExportOperationsFactory<AlternativeTestRenderingContext>() {

    /**
     * atf - Alternative Test Format ;)
     */
    override fun getTabulationFormat() = format("atf", AlternativeTestRenderingContext::class.java)

    override fun provideExportOperations(): OperationsBuilder<AlternativeTestRenderingContext>.() -> Unit = { }

    override fun createOutputBindings(): List<OutputBinding<AlternativeTestRenderingContext, *>> = listOf(AlternativeTestOutputBinding())

}