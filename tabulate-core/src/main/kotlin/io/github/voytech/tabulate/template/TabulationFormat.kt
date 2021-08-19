package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.spi.Identifiable

/**
 * Simple class representing the format of output file.
 * @property formatId - String value representing file type. e.g. xlsx, pdf, csv, txt
 * @author Wojciech Mąka
 */
class TabulationFormat(
    private val formatId: String
    ) : Identifiable {
    override fun getFormat(): String = formatId
}