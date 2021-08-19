package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.FlushingResultProvider
import io.github.voytech.tabulate.template.result.ResultProvider

class NoContext : RenderingContext
class ExampleContext : RenderingContext

fun interface AttributedCellTest {
    fun test(context: AttributedCell)
}

class TestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, NoContext>() {

    override fun getFormat() = "test"

    override fun createTableExportOperation(): TableExportOperations<T> = object: TableExportOperations<T> {

        override fun renderRowCell(context: AttributedCell) {
            test.test(context)
        }

    }

    override fun createRenderingContext(): NoContext = NoContext()

    override fun createResultProviders(): List<ResultProvider<NoContext>> = listOf(
        FlushingResultProvider { _: NoContext, _: Unit -> println("flush results") }
    )

    companion object {
        lateinit var test: AttributedCellTest
    }

}

class AnotherTestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, ExampleContext>() {

    override fun getFormat() = "mock-2"

    override fun createTableExportOperation(): TableExportOperations<T> = object: TableExportOperations<T> {

        override fun renderRowCell(context: AttributedCell) {
            println("cell context: $context")
        }

    }

    override fun createRenderingContext(): ExampleContext = ExampleContext()

    override fun createResultProviders(): List<ResultProvider<ExampleContext>> = listOf(
        FlushingResultProvider { _: ExampleContext, _: Unit -> println("flush results") }
    )

}