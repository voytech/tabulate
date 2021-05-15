package io.github.voytech.tabulate.utils

import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory

object Mocks {
    fun <T> mock(): ExportOperationsConfiguringFactory<Unit, T, Unit> = MockExportOperationsConfiguringFactory()
}
