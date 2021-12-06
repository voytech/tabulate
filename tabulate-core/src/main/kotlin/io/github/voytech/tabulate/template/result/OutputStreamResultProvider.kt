package io.github.voytech.tabulate.template.result

import io.github.voytech.tabulate.template.context.RenderingContext
import java.io.OutputStream

abstract class OutputStreamResultProvider<CTX: RenderingContext> : ResultProvider<CTX,OutputStream> {

    lateinit var outputStream: OutputStream

    lateinit var renderingContext: CTX

    final override fun outputClass(): Class<OutputStream> = OutputStream::class.java

    final override fun setOutput(renderingContext: CTX, output: OutputStream) {
        outputStream = output
        this.renderingContext = renderingContext
        onOutput(outputStream)
    }

    final override fun flush() {
        flush(outputStream)
    }

    open fun onOutput(output: OutputStream) { }

    abstract fun flush(output: OutputStream)

}