package pl.voytech.exporter.core.api.builder.dsl

import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.template.TableExportTemplate
import pl.voytech.exporter.core.template.operations.ExportOperations
import java.io.OutputStream
import pl.voytech.exporter.core.api.builder.dsl.table as T

@DslMarker
annotation class OperationMarker

@JvmSynthetic
fun <T> Collection<T>.export(stream: OutputStream, block: ExportBuilder<T>.() -> Unit) {
    ExportBuilder(this).apply(block).execute(stream)
}

@OperationMarker
class ExportBuilder<T>(private val collection: Collection<T>) {
    @JvmSynthetic
    lateinit var table: TableBuilder<T>
    @JvmSynthetic
    lateinit var operations: ExportOperations<T>

    @JvmSynthetic
    fun table(block: TableBuilderApi<T>.() -> Unit) {
        table = T(block)
    }

    @JvmSynthetic
    fun execute(stream: OutputStream) {
        TableExportTemplate(operations).export(table, collection, stream)
    }

}