package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.result.OutputBinding


/**
 * Service provider interface enabling third party table exporters.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface OutputBindingsProvider<CTX: RenderingContext> : Identifiable<CTX> {

    /**
     * Creates rendering context aware output binding. Output binding instance takes responsibility of conveying
     * rendering context representation into specific output.
     * This decouples flushing from actual table state management while performing rendering/exporting.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun createOutputBindings(): List<OutputBinding<CTX, *>>
}

fun <CTX: RenderingContext> OutputBindingsProvider<CTX>.getRenderingContextClass() : Class<CTX> =
    getDocumentFormat().provider.renderingContextClass
