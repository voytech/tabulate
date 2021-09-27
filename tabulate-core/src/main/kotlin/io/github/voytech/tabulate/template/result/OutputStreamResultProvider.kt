package io.github.voytech.tabulate.template.result

import java.io.OutputStream

abstract class OutputStreamResultProvider : ResultProvider<OutputStream> {

    lateinit var outputStream: OutputStream

    final override fun outputClass(): Class<OutputStream> = OutputStream::class.java

    final override fun setOutput(output: OutputStream) {
        outputStream = output
        onOutput(outputStream)
    }

    final override fun flush() {
        flush(outputStream)
    }

    open fun onOutput(output: OutputStream) { }

    abstract fun flush(output: OutputStream)

}