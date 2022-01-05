package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.AttributedContextExportOperations
import io.github.voytech.tabulate.template.result.OutputBinding

/**
 * Service provider interface enabling third party table exporters.
 * @author Wojciech Mąka
 */
interface ExportOperationsProvider<CTX: RenderingContext> : Identifiable {

    /**
     * @return third party API aware class to transform current context into third party tabular representation.
     * @author Wojciech Mąka
     */
    fun getContextClass(): Class<CTX>

    /**
     * Creates new instance of rendering context. Must be called before every export in order to create clean state to work on.
     * @author Wojciech Mąka
     */
    fun createRenderingContext(): CTX

    /**
     * Creates export operations working on attributed contexts (table, row, column, cell).
     * Those export operations communicates with third party exporter via rendering context.
     * @author Wojciech Mąka
     */
    fun createExportOperations(): AttributedContextExportOperations<CTX>

    /**
     * Creates rendering context aware output binding. Output binding instance takes responsibility of conveying
     * rendering context representation into specific output.
     * This decouples flushing from actual table state management while performing rendering/exporting.
     * @author Wojciech Mąka
     */
    fun createOutputBindings(): List<OutputBinding<CTX,*>>
}