package io.github.voytech.tabulate.template

/**
 * Simple class representing the format of output file.
 * @property id - String value representing file type. e.g. xlsx, pdf, csv, txt
 * @property provider - String value representing export operations implementor. e.g. 'poi' for Apache POI
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TabulationFormat(
    val id: String,
    val provider: String? = null,
    // val enablesCustomAttributeOperations
) {
    companion object {
        /**
         * Static method creating TabulationFormat using formatId and export operations provider.
         * @author
         * @since 0.1.0
         */
        @JvmStatic
        fun format(formatId: String, provider: String? = null): TabulationFormat = TabulationFormat(formatId, provider)

        /**
         * Static method creating TabulationFormat using only formatId.
         * @author
         * @since 0.1.0
         */
        @JvmStatic
        fun format(formatId: String): TabulationFormat = TabulationFormat(formatId)
    }
}