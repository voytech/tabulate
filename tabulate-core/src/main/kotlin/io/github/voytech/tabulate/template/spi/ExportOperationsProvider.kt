package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.ResultProvider

interface ExportOperationsProvider<CTX: RenderingContext,T> : Identifiable {
    fun getContextClass(): Class<CTX>

    /**
     * Create new instance of rendering context. Must be called before each export to ensure its not working on dirty state.
     * @author Wojciech Mąka
     */
    fun createRenderingContext(): CTX
    fun createExportOperations(): TableExportOperations<T,CTX>
    fun createResultProviders(): List<ResultProvider<CTX,*>>
}