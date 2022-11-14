package io.github.voytech.tabulate.core.template.spi

import io.github.voytech.tabulate.core.model.UnconstrainedModel
import io.github.voytech.tabulate.core.template.RenderingContext

/**
 * Service provider interface enabling third party table exporters.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
interface MeasureOperationsProvider<CTX: RenderingContext, M: UnconstrainedModel<M>> : Identifiable<CTX>, ModelAware<M> {

    /**
     * Creates measure operations working on attributed renderable contexts.
     * Those operations communicate with 3rd party library via rendering context in order to get size of renderable contexts.
     * @author Wojciech Mąka
     * @since 0.2.0
     */
    fun provideMeasureOperations(): BuildOperations<CTX>
}