package io.github.voytech.tabulate.template.spi

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.AttributeRenderOperationsFactory

/**
 * Service provider interface for extending existing third party exporter with additional custom attributes.
 * @author Wojciech Mąka
 */
interface AttributeRenderOperationsProvider<T, CTX: RenderingContext> {

    /**
     * @return Factory for providing custom attribute operations compatible with particular [RenderingContext].
     */
    fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<CTX,T>

    /**
     * @return Compatible [RenderingContext] class.
     */
    fun getContextClass(): Class<CTX>
}