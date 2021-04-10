package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.template.operations.ExportOperationConfiguringFactory
import pl.voytech.exporter.core.template.spi.ExportOperationFactoryProvider

class ExcelTabulateProvider : ExportOperationFactoryProvider {

    override fun <T> create(): ExportOperationConfiguringFactory<T> = apachePoiExcelExportFactory()

    override fun id(): String = ID

    companion object {
        const val ID = "xlsx"
    }
}