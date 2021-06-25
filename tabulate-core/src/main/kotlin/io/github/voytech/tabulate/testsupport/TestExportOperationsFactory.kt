package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.FlushingResultProvider
import io.github.voytech.tabulate.template.result.ResultProvider

class NoContext : RenderingContext

class TestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, NoContext>() {

    override fun getFormat() = "mock"

    override fun createTableExportOperation(): TableExportOperations<T> = object: TableExportOperations<T> {

        override fun renderRowCell(context: AttributedCell) {
            println("cell context: $context")
        }

    }

    override fun createRenderingContext(): NoContext = NoContext()

    override fun getResultProviders(): List<ResultProvider<NoContext>> = listOf(
        FlushingResultProvider { _: NoContext, _: Unit -> println("flush results") }
    )

}