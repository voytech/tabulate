package pl.voytech.exporter.utils

import pl.voytech.exporter.core.template.operations.ExportOperationsConfiguringFactory

object Mocks {
    fun <T> mock(): ExportOperationsConfiguringFactory<Unit, T, Unit> = MockExportOperationsConfiguringFactory()
}
