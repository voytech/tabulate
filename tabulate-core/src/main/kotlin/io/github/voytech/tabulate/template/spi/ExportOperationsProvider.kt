package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.AttributedContextExportOperations
import io.github.voytech.tabulate.template.result.ResultProvider

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
     * Creates rendering context aware result provider. Result provider instance creates a binding between rendering context
     * and output type, then it flushes binary/text representation managed by rendering context into output.
     * This allows to decouple flushing from actual table-representing state adaptation flow.
     * (State adaptation from `tabulate` to third party exporter)
     * @author Wojciech Mąka
     */
    fun createResultProviders(): List<ResultProvider<CTX,*>>
}