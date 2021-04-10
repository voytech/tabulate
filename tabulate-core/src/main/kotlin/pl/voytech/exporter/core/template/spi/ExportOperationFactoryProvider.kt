package pl.voytech.exporter.core.template.spi

import pl.voytech.exporter.core.template.operations.ExportOperationConfiguringFactory

interface ExportOperationFactoryProvider {
    fun <T> create() : ExportOperationConfiguringFactory<T>
    fun id(): String
}