package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.AttributeRenderOperationsFactory

/**
 * Service provider interface for extending existing third party exporter with additional custom attributes.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface AttributeRenderOperationsProvider<CTX: RenderingContext> {

    /**
     * @return Factory for providing custom attribute operations compatible with particular [RenderingContext].
     * @since 0.1.0
     * @author Wojciech Mąka
     */
    fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<CTX>

    /**
     * @return Compatible [RenderingContext] class.
     * @since 0.1.0
     * @author Wojciech Mąka
     */
    fun getContextClass(): Class<CTX>
}