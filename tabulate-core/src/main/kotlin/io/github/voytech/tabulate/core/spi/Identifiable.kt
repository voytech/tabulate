package io.github.voytech.tabulate.core.spi

import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.RenderingContext


/**
 * DocumentFormat descriptor. Defines what type is the resulting table file and what third party exporter it uses.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface Identifiable<CTX: RenderingContext> {
    fun getDocumentFormat(): DocumentFormat<CTX>
}

fun <CTX: RenderingContext> Identifiable<CTX>.getRenderingContextClass(): Class<CTX> = getDocumentFormat().provider.renderingContextClass

/**
 * RenderingContextAware descriptor. Defines what type is the resulting table file and what third party exporter it uses.
 * @author Wojciech Mąka
 * @since 0.3.0
 */
interface RenderingContextAware<CTX: RenderingContext> {
    fun getRenderingContextClass(): Class<CTX>
}

/**
 * RootModelAware descriptor. Defines what type is the resulting table file and what third party exporter it uses.
 * @author Wojciech Mąka
 * @since 0.3.0
 */
interface ModelAware<M: AbstractModel> {
    fun getModelClass(): Class<M>
}