package pl.voytech.exporter.core.template.spi

 import pl.voytech.exporter.core.template.operations.ExportOperationConfiguringFactory
 import java.util.function.Predicate

interface ExportOperationFactoryProvider: Predicate<Identifiable> {
    fun <T> create() : ExportOperationConfiguringFactory<T>
}