package io.github.voytech.tabulate.csv

import io.github.voytech.tabulate.csv.attributes.CellSeparatorCharacterAttribute
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.result.OutputStreamOutputBinding
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import java.io.BufferedWriter
import java.io.OutputStream

/**
 * Default binding of [CsvRenderingContext] to [OutputStream]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CsvOutputStreamOutputBinding : OutputStreamOutputBinding<CsvRenderingContext>() {

    override fun onBind(renderingContext: CsvRenderingContext, output: OutputStream) {
        renderingContext.doBind(output)
    }

    override fun flush(output: OutputStream) {
        renderingContext.finish()
        output.close()
    }
}

/**
 * CSV rendering context holding required state to be shared by all compatible renderers.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
open class CsvRenderingContext: RenderingContext {
    private lateinit var bufferedWriter: BufferedWriter
    private val line = StringBuilder()

    fun doBind(output: OutputStream) {
        bufferedWriter = output.bufferedWriter()
    }

    fun startRow() {
        line.clear()
    }

    private fun AttributedCell.getSeparatorCharacter(): String =
        attributes?.get(CellSeparatorCharacterAttribute::class.java)?.separator ?: ","

    fun <T> endRow(context: AttributedRowWithCells<T>) {
        val lastIndex = context.rowCellValues.size - 1
        context.rowCellValues.values.forEachIndexed { index, cell ->
            line.append(cell.value.value.toString())
            if (index < lastIndex) line.append(cell.getSeparatorCharacter())
        }
        bufferedWriter.write(line.toString())
        bufferedWriter.newLine()
    }

    fun finish() {
        bufferedWriter.close()
    }
}

/**
 * Simple .csv export operations provider implementation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CsvExportOperationsFactory: ExportOperationsProvider<CsvRenderingContext> {

    override fun getContextClass(): Class<CsvRenderingContext> = CsvRenderingContext::class.java

    override fun createRenderingContext() = CsvRenderingContext()

    override fun supportsFormat(): TabulationFormat = format("csv")

    override fun createExportOperations(): AttributedContextExportOperations<CsvRenderingContext> = object : AttributedContextExportOperations<CsvRenderingContext> {

        override fun createTable(renderingContext: CsvRenderingContext, context: AttributedTable) {

        }

        override fun beginRow(renderingContext: CsvRenderingContext, context: AttributedRow) {
            renderingContext.startRow()
        }

        override fun renderRowCell(renderingContext: CsvRenderingContext, context: AttributedCell) {

        }

        override fun <T> endRow(renderingContext: CsvRenderingContext, context: AttributedRowWithCells<T>) {
            renderingContext.endRow(context)
        }
    }

    override fun createOutputBindings(): List<OutputBinding<CsvRenderingContext, *>> = listOf(CsvOutputStreamOutputBinding())

}