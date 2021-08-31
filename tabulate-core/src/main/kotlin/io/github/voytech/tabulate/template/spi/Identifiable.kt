package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.TabulationFormat

interface Identifiable {
    fun supportsFormat(): TabulationFormat
}