package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.template.result.ResultProvider
import java.util.logging.Logger

class TestResultProvider: ResultProvider<TestRenderingContext, Unit> {

    override fun outputClass() = Unit.javaClass

    override fun flush() {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    override fun setOutput(renderingContext: TestRenderingContext, output: Unit) {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    companion object {
        val logger: Logger = Logger.getLogger(TestResultProvider::class.java.name)
    }
}

class Test2ResultProvider: ResultProvider<ExampleContext, Unit> {

    override fun outputClass() = Unit.javaClass

    override fun flush() {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    override fun setOutput(renderingContext: ExampleContext, output: Unit) {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    companion object {
        val logger: Logger = Logger.getLogger(TestResultProvider::class.java.name)
    }
}