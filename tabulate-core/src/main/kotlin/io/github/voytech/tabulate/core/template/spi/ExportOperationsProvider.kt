package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.Operations


/**
 * Service provider interface enabling third party table exporters.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface ExportOperationsProvider<CTX: RenderingContext, MDL: Model<MDL>> : Identifiable<CTX> {

    /**
     * Creates export operations working on attributed contexts (table, row, column, cell).
     * Those export operations communicates with third party exporter via rendering context.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun createExportOperations(): Operations<CTX>
    fun getModelClass(): Class<MDL>
}

/**
 * Creates new instance of rendering context. Must be called before every export in order to create clean state to work on.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun <CTX: RenderingContext, MDL: Model<MDL>> ExportOperationsProvider<CTX, MDL>.createRenderingContext() : CTX =
    getDocumentFormat().provider.renderingContextClass.newInstance()
