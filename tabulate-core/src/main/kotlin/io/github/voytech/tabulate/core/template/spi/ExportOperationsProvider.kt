package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder


typealias BuildOperations<CTX> = OperationsBuilder<CTX>.() -> Unit

/**
 * Service provider interface enabling third party table exporters.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface ExportOperationsProvider<CTX: RenderingContext, M: AbstractModel<M>> : Identifiable<CTX>, ModelAware<M> {

    /**
     * Creates export operations working on attributed contexts (table, row, column, cell).
     * Those export operations communicates with 3rd party library via rendering context.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun provideExportOperations(): BuildOperations<CTX>
}

