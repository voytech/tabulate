package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext

/**
 * TabulationFormat descriptor. Defines what type is the resulting table file and what third party exporter it uses.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface Identifiable<CTX: RenderingContext> {
    fun getTabulationFormat(): TabulationFormat<CTX>
}