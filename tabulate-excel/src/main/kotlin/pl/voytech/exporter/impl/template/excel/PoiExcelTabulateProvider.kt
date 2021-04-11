package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.template.operations.ExportOperationConfiguringFactory
import pl.voytech.exporter.core.template.spi.ExportOperationFactoryProvider

class PoiExcelTabulateProvider : ExportOperationFactoryProvider {

    override fun <T> create(): ExportOperationConfiguringFactory<T> = PoiExcelExportOperationsFactory()

    override fun id(): String = ID

    companion object {
        const val ID = "xlsx"
    }
}