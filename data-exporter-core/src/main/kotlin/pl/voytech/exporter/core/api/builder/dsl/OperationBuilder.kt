package pl.voytech.exporter.core.api.builder.dsl

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.DataExportTemplate
import pl.voytech.exporter.core.template.operations.ExportOperations
import java.io.OutputStream

@DslMarker
annotation class OperationMarker

@JvmSynthetic
fun <T> Collection<T>.export(stream: OutputStream, block: ExportBuilder<T>.() -> Unit) {
    ExportBuilder(this).apply(block).execute(stream)
}

@OperationMarker
class ExportBuilder<T>(private val collection: Collection<T>) {
    @JvmSynthetic
    lateinit var table: Table<T>
    @JvmSynthetic
    lateinit var operations: ExportOperations<T>

    @JvmSynthetic
    fun table(block: TableBuilder<T>.() -> Unit) {
        table = TableBuilder.new<T>().apply(block).build()
    }

    @JvmSynthetic
    fun execute(stream: OutputStream) {
        DataExportTemplate(operations).export(table, collection, stream)
    }

}
