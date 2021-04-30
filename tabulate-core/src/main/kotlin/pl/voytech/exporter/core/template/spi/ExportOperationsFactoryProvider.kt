package pl.voytech.exporter.core.template.spi

 import pl.voytech.exporter.core.template.operations.ExportOperationsConfiguringFactory
 import java.util.function.Predicate

interface ExportOperationsFactoryProvider<T, O>: Predicate<Identifiable> {
    fun create() : ExportOperationsConfiguringFactory<T, O>
}