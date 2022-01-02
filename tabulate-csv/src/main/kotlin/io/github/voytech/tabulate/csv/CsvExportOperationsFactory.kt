package io.github.voytech.tabulate.csv

import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.OutputStreamResultProvider
import io.github.voytech.tabulate.template.result.ResultProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.OutputStream

class CsvOutputStreamResultProvider : OutputStreamResultProvider<CsvRenderingContext>() {

    override fun onBind(renderingContext: CsvRenderingContext, output: OutputStream) {
        renderingContext.doBind(output)
    }

    override fun flush(output: OutputStream) {
        output.flush()
        output.close()
    }
}

class CsvRenderingContext: RenderingContext {
    private lateinit var bufferedOutputStream: BufferedOutputStream
    private lateinit var bufferedWriter: BufferedWriter
    private val line = StringBuilder()

    fun doBind(output: OutputStream) {
        bufferedOutputStream = output.buffered()
        bufferedWriter = bufferedOutputStream.bufferedWriter()
    }

    fun startRow() {
        line.clear()
    }

    fun <T> endRow(context: AttributedRowWithCells<T>) {
        val lastIndex = context.rowCellValues.size - 1
        context.rowCellValues.values.forEachIndexed { index, cell ->
            line.append(cell.value.value.toString())
            if (index < lastIndex) line.append(",")
        }
        bufferedWriter.write(line.toString())
        bufferedWriter.newLine()
    }
}

class CsvExportOperationsFactory: ExportOperationsProvider<CsvRenderingContext> {

    override fun getContextClass(): Class<CsvRenderingContext> = CsvRenderingContext::class.java

    override fun createRenderingContext() = CsvRenderingContext()

    override fun supportsFormat(): TabulationFormat = TabulationFormat.format("csv")

    override fun createExportOperations(): AttributedContextExportOperations<CsvRenderingContext> = object : AttributedContextExportOperations<CsvRenderingContext> {

        override fun createTable(renderingContext: CsvRenderingContext, context: AttributedTable) { }

        override fun beginRow(renderingContext: CsvRenderingContext, context: AttributedRow) {
            renderingContext.startRow()
        }

        override fun renderRowCell(renderingContext: CsvRenderingContext, context: AttributedCell) { }

        override fun <T> endRow(renderingContext: CsvRenderingContext, context: AttributedRowWithCells<T>) {
            renderingContext.endRow(context)
        }
    }

    override fun createResultProviders(): List<ResultProvider<CsvRenderingContext, *>> = listOf(CsvOutputStreamResultProvider())

}