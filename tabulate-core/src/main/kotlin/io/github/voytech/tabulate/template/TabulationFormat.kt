package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.spi.Identifiable

class TabulationFormat<T,O>(
    private val formatId: String,
    val resultHandler: ResultHandler<T,O>
    ) : Identifiable {

    override fun getFormat(): String = formatId

}