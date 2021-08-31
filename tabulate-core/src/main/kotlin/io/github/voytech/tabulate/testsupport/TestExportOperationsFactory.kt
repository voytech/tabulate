package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.ResultProvider

class NoContext : RenderingContext
class ExampleContext : RenderingContext

fun interface AttributedCellTest {
    fun test(context: AttributedCell)
}

class TestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, NoContext>() {

    override fun supportsFormat() = format("test")

    override fun createTableExportOperations(): TableExportOperations<T> = object: TableExportOperations<T> {

        override fun renderRowCell(context: AttributedCell) {
            test.test(context)
        }

    }

    override fun createRenderingContext(): NoContext = NoContext()

    override fun createResultProviders(): List<ResultProvider<*>> = listOf(NoResultProvider())

    companion object {
        lateinit var test: AttributedCellTest
    }

}

class CompetingTestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, ExampleContext>() {

    override fun supportsFormat() = format("test-2")

    override fun createTableExportOperations(): TableExportOperations<T> = object: TableExportOperations<T> {

        override fun renderRowCell(context: AttributedCell) {
            println("cell context: $context")
        }

    }

    override fun createRenderingContext(): ExampleContext = ExampleContext()

    override fun createResultProviders(): List<ResultProvider<*>> = listOf(NoResultProvider())

}