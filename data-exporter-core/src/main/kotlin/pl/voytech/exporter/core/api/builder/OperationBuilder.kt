package pl.voytech.exporter.core.api.builder

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.DataExportTemplate
import pl.voytech.exporter.core.template.ExportOperations
import java.io.OutputStream

@DslMarker
annotation class OperationMarker

@JvmSynthetic
fun <T, A> Collection<T>.export(stream: OutputStream, block: ExportBuilder<T, A>.() -> Unit) {
    ExportBuilder<T, A>(this).apply(block).execute(stream)
}

@JvmSynthetic
fun <T, A> Collection<T>.export(block: ExportBuilder<T, A>.() -> Unit) {
    ExportBuilder<T, A>(this).apply(block).execute()
}

@OperationMarker
class ExportBuilder<T, A>(private val collection: Collection<T>) {
    @JvmSynthetic
    lateinit var table: Table<T>
    @JvmSynthetic
    lateinit var operations: ExportOperations<T, A>

    @JvmSynthetic
    fun table(block: TableBuilder<T>.() -> Unit) {
        table = TableBuilder<T>().apply(block).build()
    }

    @JvmSynthetic
    fun execute(stream: OutputStream) {
        DataExportTemplate(operations).export(table, collection, stream)
    }

    @JvmSynthetic
    fun execute() {
        DataExportTemplate(operations).export(table, collection)
    }
}