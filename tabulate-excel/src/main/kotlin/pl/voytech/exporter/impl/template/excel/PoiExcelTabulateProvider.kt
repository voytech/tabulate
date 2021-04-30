package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.template.operations.ExportOperationsConfiguringFactory
import pl.voytech.exporter.core.template.spi.ExportOperationsFactoryProvider
import pl.voytech.exporter.core.template.spi.Identifiable
import java.io.OutputStream

class PoiExcelTabulateProvider<T> : ExportOperationsFactoryProvider<T, OutputStream> {

    override fun create(): ExportOperationsConfiguringFactory<T, OutputStream> = PoiExcelExportOperationsFactory()

    override fun test(t: Identifiable): Boolean = ID == t.getFormat()

    companion object {
        const val ID = "xlsx"
    }
}