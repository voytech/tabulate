package io.github.voytech.tabulate.utils

import io.github.voytech.tabulate.template.ResultHandler
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations
import org.reactivestreams.Publisher

class MockExportOperationsConfiguringFactory<T> : ExportOperationsConfiguringFactory<Unit, T, Unit>() {

    override fun getFormat() = "mock"

    override fun createRenderingContext() { }

    override fun createTableExportOperation(): TableExportOperations<T, Unit> = object: TableExportOperations<T, Unit> {
        override fun initialize(source: Publisher<T>, resultHandler: ResultHandler<T, Unit>) {
            println("Do nothing. Mock implementation")
        }

        override fun renderRowCell(context: AttributedCell) {
            println("cell context: $context")
        }

        override fun finish() {
            println("Do nothing. Mock implementation")
        }

    }

}