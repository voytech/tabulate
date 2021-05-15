package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.template.spi.Identifiable

class TabulationFormat<T,O>(
    private val formatId: String,
    val resultHandler: ResultHandler<T,O>
    ) : Identifiable {

    override fun getFormat(): String = formatId

}