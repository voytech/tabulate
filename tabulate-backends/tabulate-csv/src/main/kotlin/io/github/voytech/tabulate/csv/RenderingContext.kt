package io.github.voytech.tabulate.csv

import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.result.OutputStreamOutputBinding
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider
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

class CsvOutputBindingsProvider : OutputBindingsProvider<CsvRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<CsvRenderingContext, *>> = listOf(
        CsvOutputStreamOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<CsvRenderingContext> = DocumentFormat.format("csv")

}

/**
 * CSV rendering context holding required state to be shared by all compatible renderers.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
open class CsvRenderingContext : RenderingContext {
    internal lateinit var bufferedWriter: BufferedWriter
    internal val line = StringBuilder()
}