package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.Operations
import io.github.voytech.tabulate.template.result.OutputBinding

/**
 * Service provider interface enabling third party table exporters.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface ExportOperationsProvider<CTX: RenderingContext> : Identifiable<CTX> {

    /**
     * Creates new instance of rendering context. Must be called before every export in order to create clean state to work on.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun createRenderingContext(): CTX = getTabulationFormat().provider.renderingContextClass.newInstance()

    /**
     * Creates export operations working on attributed contexts (table, row, column, cell).
     * Those export operations communicates with third party exporter via rendering context.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun createExportOperations(): Operations<CTX>

    /**
     * Creates rendering context aware output binding. Output binding instance takes responsibility of conveying
     * rendering context representation into specific output.
     * This decouples flushing from actual table state management while performing rendering/exporting.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun createOutputBindings(): List<OutputBinding<CTX,*>>
}

fun <CTX: RenderingContext> ExportOperationsProvider<CTX>.getRenderingContextClass() : Class<CTX> =
    getTabulationFormat().provider.renderingContextClass