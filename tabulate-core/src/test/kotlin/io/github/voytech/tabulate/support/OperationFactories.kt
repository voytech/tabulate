package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.ResultProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider

class ExampleContext : RenderingContext

fun interface AttributedCellTest {
    fun test(context: AttributedCell)
}

interface AttributedRowTest {
    fun <T> test(context: AttributedRowWithCells<T>) {}
}

fun interface AttributedColumnTest {
    fun test(context: AttributedColumn)
}

class TestExportOperationsFactory : ExportOperationsProvider<TestRenderingContext> {

    override fun supportsFormat() = format("test")

    override fun createExportOperations(): AttributedContextExportOperations<TestRenderingContext> =
        object : AttributedContextExportOperations<TestRenderingContext> {

            override fun renderColumn(renderingContext: TestRenderingContext, context: AttributedColumn) {
                columnTest?.test(context)
            }

            override fun renderRowCell(renderingContext: TestRenderingContext, context: AttributedCell) {
                cellTest?.test(context)
            }

            override fun beginRow(renderingContext: TestRenderingContext, context: AttributedRow) {
                println("begin row: $context")
            }

            override fun <T> endRow(renderingContext: TestRenderingContext, context: AttributedRowWithCells<T>) {
                rowTest?.test(context)
            }

            override fun createTable(renderingContext: TestRenderingContext, context: AttributedTable) {
                println("table context: $context")
            }

        }

    override fun createResultProviders(): List<ResultProvider<TestRenderingContext, *>> = listOf(
        TestResultProvider(), OutputStreamTestResultProvider()
    )

    override fun getContextClass(): Class<TestRenderingContext> = TestRenderingContext::class.java

    override fun createRenderingContext(): TestRenderingContext = TestRenderingContext().also {
        CURRENT_RENDERING_CONTEXT_INSTANCE = it
    }

    companion object {
        @JvmStatic
        var cellTest: AttributedCellTest? = null

        @JvmStatic
        var rowTest: AttributedRowTest? = null

        @JvmStatic
        var columnTest: AttributedColumnTest? = null

        @JvmStatic
        var CURRENT_RENDERING_CONTEXT_INSTANCE: TestRenderingContext? = null

        fun clear() {
            cellTest = null
            rowTest = null
            columnTest = null
        }
    }

}

class TestEnabledAttributes : AttributeRenderOperationsFactory<TestRenderingContext> {
    override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<TestRenderingContext, out CellAttribute>> =
        setOf(
            CellTextStylesAttributeTestRenderOperation(Spy.spy),
            CellBordersAttributeTestRenderOperation(Spy.spy, -1),
            CellBackgroundAttributeTestRenderOperation(Spy.spy),
            CellAlignmentAttributeTestRenderOperation(Spy.spy)
        )

    override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<TestRenderingContext, out ColumnAttribute>> =
        setOf(ColumnWidthAttributeTestRenderOperation(Spy.spy))

    override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<TestRenderingContext, out RowAttribute>> =
        setOf(RowHeightAttributeTestRenderOperation(Spy.spy))

    override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<TestRenderingContext, out TableAttribute>> =
        setOf(TemplateFileAttributeTestRenderOperation(Spy.spy))
}

class AttributedTestExportOperationsFactory : ExportOperationsConfiguringFactory<TestRenderingContext>() {

    override fun provideExportOperations(): TableExportOperations<TestRenderingContext> =
        TableExportTestOperations(Spy.spy)

    override fun createResultProviders(): List<ResultProvider<TestRenderingContext, *>> = listOf(
        TestResultProvider(), OutputStreamTestResultProvider()
    )

    override fun getContextClass(): Class<TestRenderingContext> = TestRenderingContext::class.java

    override fun createRenderingContext(): TestRenderingContext = TestRenderingContext()

    override fun supportsFormat(): TabulationFormat = format("spy")

    override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<TestRenderingContext> =
        TestEnabledAttributes()

}

class CompetingTestExportOperationsFactory : ExportOperationsConfiguringFactory<ExampleContext>() {

    override fun supportsFormat() = format("test-2")

    override fun provideExportOperations(): TableExportOperations<ExampleContext> =
        object : TableExportOperations<ExampleContext> {
            override fun renderRowCell(renderingContext: ExampleContext, context: RowCellContext) {
                println("cell context: $context")
            }

            override fun createTable(renderingContext: ExampleContext, context: TableContext) {
                println("table context: $context")
            }
        }

    override fun createRenderingContext(): ExampleContext = ExampleContext()

    override fun getContextClass(): Class<ExampleContext> = ExampleContext::class.java

    override fun createResultProviders(): List<ResultProvider<ExampleContext, *>> = listOf(Test2ResultProvider())

}