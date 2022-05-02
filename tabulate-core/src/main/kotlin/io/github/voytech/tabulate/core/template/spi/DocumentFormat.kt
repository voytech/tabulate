package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.template.RenderingContext

data class ExportProvider<CTX : RenderingContext>(
    val providerId: String? = "default",
    val renderingContextClass: Class<CTX>
)

class DocumentFormat<CTX : RenderingContext>(
    val id: String,
    val provider: ExportProvider<CTX>
) {
    companion object {
        /**
         * Static method creating ExportFormat using formatId and export operations provider.
         * @author
         * @since 0.1.0
         */
        @JvmSynthetic
        inline fun <reified CTX : RenderingContext> format(
            formatId: String, provider: String? = null
        ): DocumentFormat<CTX> = DocumentFormat(formatId, ExportProvider(provider, CTX::class.java))

        @JvmStatic
        fun <CTX : RenderingContext> format(
            formatId: String,
            renderingContext: Class<CTX>,
            provider: String? = null
        ): DocumentFormat<CTX> = DocumentFormat(formatId, ExportProvider(provider, renderingContext))

    }
}