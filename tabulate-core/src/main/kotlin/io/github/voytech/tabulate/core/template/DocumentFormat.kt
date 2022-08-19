package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.template.exception.UnknownDocumentFormatException
import java.io.File

/**
 * Simple class representing the format of output file.
 * @property id - String value representing file type. e.g. xlsx, pdf, csv, txt
 * @property provider - String value representing export operations implementor. e.g. 'poi' for Apache POI
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class DocumentFormat(
    val id: String,
    val provider: String? = null,
) {
    companion object {
        /**
         * Static method creating DocumentFormat using formatId and export operations provider.
         * @author
         * @since 0.1.0
         */
        @JvmStatic
        fun format(formatId: String, provider: String? = null): DocumentFormat = DocumentFormat(formatId, provider)

        /**
         * Static method creating DocumentFormat using only formatId.
         * @author
         * @since 0.1.0
         */
        @JvmStatic
        fun format(formatId: String): DocumentFormat = DocumentFormat(formatId)
    }
}

fun File.documentFormat(provider: String? = null) =
    if (extension.isNotBlank()) {
        DocumentFormat(extension, provider)
    } else throw UnknownDocumentFormatException()