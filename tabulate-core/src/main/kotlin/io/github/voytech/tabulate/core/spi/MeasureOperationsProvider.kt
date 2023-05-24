package io.github.voytech.tabulate.core.spi

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.RenderingContext

/**
 * Service provider interface enabling measuring operations provided by third-party [RenderingContext].
 * @author Wojciech Mąka
 * @since 0.2.0
 */
interface MeasureOperationsProvider<CTX: RenderingContext, M: AbstractModel> : Identifiable<CTX>, ModelAware<M> {

    /**
     * Creates measure operations working on attributed renderable contexts.
     * Those operations communicate with 3rd party library via rendering context in order to get size of renderable contexts.
     * @author Wojciech Mąka
     * @since 0.2.0
     */
    fun provideMeasureOperations(): BuildOperations<CTX>
}