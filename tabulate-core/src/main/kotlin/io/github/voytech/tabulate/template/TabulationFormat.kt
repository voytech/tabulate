package io.github.voytech.tabulate.template

/**
 * Simple class representing the format of output file.
 * @property id - String value representing file type. e.g. xlsx, pdf, csv, txt
 * @property provider - String value representing export operations implementor.
 * @author Wojciech Mąka
 */
class TabulationFormat(
    val id: String,
    val provider: String? = null,
    // val enablesCustomAttributeOperations
) {
    companion object {
        @JvmStatic
        fun format(formatId: String, provider: String? = null): TabulationFormat = TabulationFormat(formatId, provider)

        @JvmStatic
        fun format(formatId: String): TabulationFormat = TabulationFormat(formatId)
    }
}