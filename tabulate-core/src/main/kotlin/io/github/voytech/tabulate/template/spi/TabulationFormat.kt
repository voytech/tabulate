package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext

/**
 * Simple class representing format of output file.
 * @property id - String value representing file type. e.g. xlsx, pdf, csv, txt
 * @property provider - represents export operations implementation associated rendering context.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TabulationFormat<CTX : RenderingContext>(
    val id: String,
    val provider: TabulationProvider<CTX>
) {
    companion object {
        /**
         * Static method creating TabulationFormat using formatId and export operations provider.
         * @author
         * @since 0.1.0
         */
        @JvmStatic
        fun <CTX: RenderingContext> format(
            formatId: String, renderingContext: Class<CTX>, provider: String? = null
        ): TabulationFormat<CTX> = TabulationFormat(formatId, TabulationProvider(provider,renderingContext))

    }
}