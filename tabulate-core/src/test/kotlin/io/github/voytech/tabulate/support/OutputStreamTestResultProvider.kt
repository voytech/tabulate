package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.template.result.ResultProvider
import java.io.OutputStream
import java.util.logging.Logger

class OutputStreamTestResultProvider: ResultProvider<TestRenderingContext, OutputStream> {
    override fun outputClass() = OutputStream::class.java
    override fun setOutput(renderingContext: TestRenderingContext, output: OutputStream) {
        logger.info("This is fake implementation of ResultProvider flushing results into OutputStream")
    }

    override fun flush() {
        logger.info("This is fake implementation of ResultProvider flushing results into OutputStream")
    }

    companion object {
        val logger: Logger = Logger.getLogger(OutputStreamTestResultProvider::class.java.name)
    }
}