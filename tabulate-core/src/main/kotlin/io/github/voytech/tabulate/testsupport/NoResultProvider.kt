package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.template.result.ResultProvider
import java.util.logging.Logger

class NoResultProvider: ResultProvider<Unit> {

    override fun outputClass() = Unit.javaClass

    override fun flush(output: Unit) {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    companion object {
        val logger: Logger = Logger.getLogger(NoResultProvider::class.java.name)
    }
}