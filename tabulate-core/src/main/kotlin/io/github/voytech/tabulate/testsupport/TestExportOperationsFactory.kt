package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.ResultProvider

class NoContext : RenderingContext
class ExampleContext : RenderingContext

fun interface AttributedCellTest {
    fun test(context: AttributedCell)
}

interface AttributedRowTest {
    fun <T> test(context: AttributedRow<T>) { }
}

class TestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, NoContext>() {

    override fun supportsFormat() = format("test")

    override fun createRenderingContext(): NoContext = NoContext()

    override fun createExportOperations(renderingContext: NoContext): TableExportOperations<T> = object: TableExportOperations<T> {
        override fun renderRowCell(context: AttributedCell) {
            cellTest?.test(context)
        }

        override fun beginRow(context: AttributedRow<T>) {
            rowTest?.test(context)
        }
    }

    override fun createResultProviders(renderingContext: NoContext): List<ResultProvider<*>> = listOf(
        NoResultProvider(), OutputStreamTestResultProvider()
    )

    companion object {
        var cellTest: AttributedCellTest? = null
        var rowTest: AttributedRowTest? = null

        fun clear() {
            cellTest = null
            rowTest = null
        }
    }

}

class CompetingTestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, ExampleContext>() {

    override fun supportsFormat() = format("test-2")

    override fun createExportOperations(renderingContext: ExampleContext): TableExportOperations<T> = object: TableExportOperations<T> {
        override fun renderRowCell(context: AttributedCell) {
            println("cell context: $context")
        }
    }

    override fun createRenderingContext(): ExampleContext = ExampleContext()

    override fun createResultProviders(renderingContext: ExampleContext): List<ResultProvider<*>> = listOf(NoResultProvider())

}