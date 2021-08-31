package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.template.result.ResultProvider

class NoResultProvider: ResultProvider<Unit> {
    override fun outputClass() = Unit.javaClass
    override fun flush(output: Unit) {}
}