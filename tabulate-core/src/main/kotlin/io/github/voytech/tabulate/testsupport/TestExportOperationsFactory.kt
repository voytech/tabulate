package io.github.voytech.tabulate.testsupport

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

class TestExportOperationsFactory<T>: ExportOperationsProvider<T> {

    override fun supportsFormat() = format("test")

    override fun createExportOperations(): TableExportOperations<T> = object: TableExportOperations<T> {

        override fun renderColumn(context: AttributedColumn) {
            columnTest?.test(context)
        }

        override fun renderRowCell(context: AttributedCell) {
            cellTest?.test(context)
        }

        override fun beginRow(context: AttributedRow<T>) {
            println("begin row: $context")
        }

        override fun endRow(context: AttributedRowWithCells<T>) {
            rowTest?.test(context)
        }

        override fun createTable(context: AttributedTable) {
            println("table context: $context")
        }

    }

    override fun createResultProviders(): List<ResultProvider<*>> = listOf(
        NoResultProvider(), OutputStreamTestResultProvider()
    )


    companion object {
        @JvmStatic
        var cellTest: AttributedCellTest? = null
        @JvmStatic
        var rowTest: AttributedRowTest? = null
        @JvmStatic
        var columnTest: AttributedColumnTest? = null

        fun clear() {
            cellTest = null
            rowTest = null
            columnTest = null
        }
    }

}

class CompetingTestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, ExampleContext>() {

    override fun supportsFormat() = format("test-2")

    override fun createExportOperations(renderingContext: ExampleContext): ExposedContextExportOperations<T> = object: ExposedContextExportOperations<T> {
        override fun renderRowCell(context: RowCellContext) {
            println("cell context: $context")
        }

        override fun createTable(context: TableContext) {
            println("table context: $context")
        }
    }

    override fun createRenderingContext(): ExampleContext = ExampleContext()

    override fun createResultProviders(renderingContext: ExampleContext): List<ResultProvider<*>> = listOf(NoResultProvider())

}