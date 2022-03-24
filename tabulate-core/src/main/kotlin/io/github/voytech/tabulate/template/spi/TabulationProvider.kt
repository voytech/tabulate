package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext

/**
 * Simple class identifying rendering operations provider.
 * @property providerId - stands for file type. e.g. xlsx, pdf, csv, txt
 * @property renderingContextClass - rendering context class implementation
 * @author Wojciech Mąka
 * @since 0.1.1
 */
data class TabulationProvider<CTX: RenderingContext>(
    val providerId: String? = "default",
    val renderingContextClass: Class<CTX>
)