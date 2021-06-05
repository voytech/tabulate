package io.github.voytech.tabulate.utils

import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.VoidRenderingContext
import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations

class MockExportOperationsConfiguringFactory<T> : ExportOperationsConfiguringFactory<T, Unit, VoidRenderingContext>() {

    override fun getFormat() = "mock"

    override fun createTableExportOperation(): TableExportOperations<T, Unit> = object: TableExportOperations<T, Unit> {

        override fun renderRowCell(context: AttributedCell) {
            println("cell context: $context")
        }

        override fun finish(result: Unit) {
            println("Do nothing. Mock implementation")
        }

    }

    override fun createRenderingContext(): VoidRenderingContext  = VoidRenderingContext()


}