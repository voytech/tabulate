package io.github.voytech.tabulate.core.result

import io.github.voytech.tabulate.core.RenderingContext
import java.io.OutputStream

abstract class OutputStreamOutputBinding<CTX: RenderingContext> : OutputBinding<CTX, OutputStream> {

    lateinit var outputStream: OutputStream

    lateinit var renderingContext: CTX

    final override fun outputClass(): Class<OutputStream> = OutputStream::class.java

    final override fun setOutput(renderingContext: CTX, output: OutputStream) {
        outputStream = output
        this.renderingContext = renderingContext
        onBind(renderingContext, outputStream)
    }

    final override fun flush() {
        flush(outputStream)
    }

    protected open fun onBind(renderingContext: CTX, output: OutputStream) { }

    abstract fun flush(output: OutputStream)

}