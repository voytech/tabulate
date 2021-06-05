package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.spi.Identifiable

class TabulationFormat(
    private val formatId: String
    ) : Identifiable {
    override fun getFormat(): String = formatId
}