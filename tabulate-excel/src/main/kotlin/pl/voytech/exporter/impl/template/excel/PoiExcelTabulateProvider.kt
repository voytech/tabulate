package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.template.operations.ExportOperationConfiguringFactory
import pl.voytech.exporter.core.template.spi.ExportOperationFactoryProvider
import pl.voytech.exporter.core.template.spi.Identifiable

class PoiExcelTabulateProvider : ExportOperationFactoryProvider {

    override fun <T> create(): ExportOperationConfiguringFactory<T> = PoiExcelExportOperationsFactory()

    override fun test(t: Identifiable): Boolean = ID == t.getIdent()

    companion object {
        const val ID = "xlsx"
    }
}