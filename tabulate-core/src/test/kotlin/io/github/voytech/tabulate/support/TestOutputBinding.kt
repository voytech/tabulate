package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.result.OutputBinding
import java.util.logging.Logger

class TestOutputBinding: OutputBinding<TestRenderingContext, Unit> {

    override fun outputClass(): Class<Unit> = reify()

    override fun flush() {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    override fun setOutput(renderingContext: TestRenderingContext, output: Unit) {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    companion object {
        val logger: Logger = Logger.getLogger(TestOutputBinding::class.java.name)
    }
}

