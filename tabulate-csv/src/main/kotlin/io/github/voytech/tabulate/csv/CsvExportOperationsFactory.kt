package io.github.voytech.tabulate.csv

import io.github.voytech.tabulate.csv.attributes.CellSeparatorCharacterAttribute
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.result.OutputStreamOutputBinding
import io.github.voytech.tabulate.template.spi.TabulationFormat
import io.github.voytech.tabulate.template.spi.TabulationFormat.Companion.format
import java.io.BufferedWriter
import java.io.OutputStream

/**
 * Default binding of [CsvRenderingContext] to [OutputStream]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CsvOutputStreamOutputBinding : OutputStreamOutputBinding<CsvRenderingContext>() {

    override fun onBind(renderingContext: CsvRenderingContext, output: OutputStream) {
        renderingContext.bufferedWriter = output.bufferedWriter()
    }

    override fun flush(output: OutputStream) {
        renderingContext.bufferedWriter.close()
        output.close()
    }
}

/**
 * CSV rendering context holding required state to be shared by all compatible renderers.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
open class CsvRenderingContext: RenderingContext {
    internal lateinit var bufferedWriter: BufferedWriter
    internal val line = StringBuilder()
}

/**
 * Simple .csv export operations provider implementation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CsvExportOperationsFactory: ExportOperationsFactory<CsvRenderingContext>() {

    override fun getTabulationFormat(): TabulationFormat<CsvRenderingContext> =
        format("csv", CsvRenderingContext::class.java)

    override fun provideExportOperations(): OperationsBuilder<CsvRenderingContext>.() -> Unit = {

        openRow = OpenRowOperation { renderingContext, _ ->
            renderingContext.line.clear()
        }

        closeRow = CloseRowOperation { renderingContext, context ->
            val lastIndex = context.rowCellValues.size - 1
            with(renderingContext) {
                context.rowCellValues.values.forEachIndexed { index, cell ->
                    line.append(cell.rawValue.toString())
                    if (index < lastIndex) line.append(cell.getSeparatorCharacter())
                }
                bufferedWriter.write(line.toString())
                bufferedWriter.newLine()
            }
        }

    }

    private fun CellContext.getSeparatorCharacter(): String =
        getModelAttribute(CellSeparatorCharacterAttribute::class.java)?.separator ?: ","

    override fun createOutputBindings(): List<OutputBinding<CsvRenderingContext, *>> = listOf(CsvOutputStreamOutputBinding())
}