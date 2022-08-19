package io.github.voytech.tabulate.core.template.operation.factories

import io.github.voytech.tabulate.core.model.UnconstrainedModel
import io.github.voytech.tabulate.core.template.DocumentFormat
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.loadExportOperationProviders
import io.github.voytech.tabulate.core.template.operation.EnableAttributeOperationAwareness
import io.github.voytech.tabulate.core.template.operation.Enhance
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.core.template.spi.getRenderingContextClass

typealias DiscoveredExportOperationFactories<R> = Map<Class<out UnconstrainedModel<*>>, List<ExportOperationsProvider<R, *>>>

private fun <R : RenderingContext> DiscoveredExportOperationFactories<R>.getRenderingContextClass(): Class<R> =
    values.first().first().getRenderingContextClass()

@Suppress("UNCHECKED_CAST")
private operator fun <R : RenderingContext, M : UnconstrainedModel<M>> DiscoveredExportOperationFactories<R>.get(model: M): List<ExportOperationsProvider<R, M>> =
    (this[model.javaClass] as? List<ExportOperationsProvider<R, M>>) ?: emptyList()

/**
 * Export operations factory that can discover attribute operations.
 * It exposes [Operations] as public API (an interface without attribute-set in operation context).
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class ExportOperationsFactory<CTX : RenderingContext>(private val documentFormat: DocumentFormat) {

    private val operationProviders: DiscoveredExportOperationFactories<CTX> by lazy {
        loadExportOperationProviders(documentFormat)
    }

    internal val renderingContext: Class<CTX> by lazy {
        operationProviders.getRenderingContextClass()
    }

    private val attributeOperationsFactory: AttributeOperationsFactory<CTX> by lazy {
        AttributeOperationsFactory(renderingContext)
    }

    fun <M : UnconstrainedModel<M>> createExportOperations(model: M, vararg customEnhancers: Enhance<CTX>): Operations<CTX> =
        operationProviders[model].fold(OperationsBuilder(renderingContext)) { builder, provider ->
            builder.apply(provider.provideExportOperations())
        }.build(
            EnableAttributeOperationAwareness(attributeOperationsFactory.createAttributeOperations(model)),
            *customEnhancers
        )

}
