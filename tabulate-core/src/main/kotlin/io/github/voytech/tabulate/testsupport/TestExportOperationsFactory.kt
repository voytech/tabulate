package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.FlushingRenderingContext
import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory
import io.github.voytech.tabulate.template.operations.TableExportOperations

class TestExportOperationsFactory<T>: ExportOperationsConfiguringFactory<T, FlushingRenderingContext<Unit>>() {

    override fun getFormat() = "mock"

    override fun createTableExportOperation(): TableExportOperations<T> = object: TableExportOperations<T> {

        override fun renderRowCell(context: AttributedCell) {
            println("cell context: $context")
        }

    }

    override fun createRenderingContext(): FlushingRenderingContext<Unit> = FlushingRenderingContext { }

}