package pl.voytech.exporter.core.api.dsl

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.DataExportTemplate
import pl.voytech.exporter.core.template.ExportOperations
import java.io.OutputStream


fun <T,A> Collection<T>.export(stream: OutputStream,block : ExportBuilder<T,A>.() -> Unit) {
    ExportBuilder<T,A>(this).apply(block).execute(stream)
}

fun <T,A> Collection<T>.export(block: ExportBuilder<T, A>.() -> Unit){
    ExportBuilder<T,A>(this).apply(block).execute()
}

class ExportBuilder<T,A>(private val collection: Collection<T>) {
    lateinit var table: Table<T>
    lateinit var operations: ExportOperations<T, A>

    fun table(block: TableBuilder<T>.() -> Unit) {
        table = TableBuilder<T>().apply(block).build()
    }

    fun execute(stream: OutputStream) {
        DataExportTemplate(operations).export(table, collection, stream)
    }

    fun execute() {
        DataExportTemplate(operations).export(table, collection)
    }
}