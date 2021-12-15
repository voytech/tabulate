package io.github.voytech.tabulate.support

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
    fun <T> test(context: AttributedRowWithCells<T>) { }
}

fun interface AttributedColumnTest {
    fun test(context: AttributedColumn)
}

class TestExportOperationsFactory<T>: ExportOperationsProvider<TestRenderingContext,T> {

    override fun supportsFormat() = format("test")

    override fun createExportOperations(): AttributedContextExportOperations<T, TestRenderingContext> = object: AttributedContextExportOperations<T, TestRenderingContext> {

        override fun renderColumn(renderingContext: TestRenderingContext, context: AttributedColumn) {
            columnTest?.test(context)
        }

        override fun renderRowCell(renderingContext: TestRenderingContext, context: AttributedCell) {
            cellTest?.test(context)
        }

        override fun beginRow(renderingContext: TestRenderingContext, context: AttributedRow<T>) {
            println("begin row: $context")
        }

        override fun endRow(renderingContext: TestRenderingContext, context: AttributedRowWithCells<T>) {
            rowTest?.test(context)
        }

        override fun createTable(renderingContext: TestRenderingContext, context: AttributedTable) {
            println("table context: $context")
        }

    }

    override fun createResultProviders(): List<ResultProvider<TestRenderingContext,*>> = listOf(
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

class CompetingTestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, ExampleContext>() {

    override fun supportsFormat() = format("test-2")

    override fun provideExportOperations(): TableExportOperations<T, ExampleContext> = object: TableExportOperations<T, ExampleContext> {
        override fun renderRowCell(renderingContext: ExampleContext, context: RowCellContext) {
            println("cell context: $context")
        }

        override fun createTable(renderingContext: ExampleContext, context: TableContext) {
            println("table context: $context")
        }
    }

    override fun createRenderingContext(): ExampleContext = ExampleContext()

    override fun getContextClass(): Class<ExampleContext> = ExampleContext::class.java

    override fun createResultProviders(): List<ResultProvider<ExampleContext,*>> = listOf(Test2ResultProvider())

}