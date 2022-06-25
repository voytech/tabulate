package io.github.voytech.tabulate.core.template.operation.factories

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.DocumentFormat
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.loadExportOperationProviders
import io.github.voytech.tabulate.core.template.operation.EnableAttributeOperationAwareness
import io.github.voytech.tabulate.core.template.operation.Enhance
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.core.template.spi.getRenderingContextClass

typealias DiscoveredExportOperationFactories<R> = Map<Class<out Model<*>>, List<ExportOperationsProvider<R, *>>>

private fun <R : RenderingContext> DiscoveredExportOperationFactories<R>.getRenderingContextClass(): Class<R> =
    values.first().first().getRenderingContextClass()


@Suppress("UNCHECKED_CAST")
private operator fun <R : RenderingContext, MDL : Model<MDL>> DiscoveredExportOperationFactories<R>.get(model: MDL): List<ExportOperationsProvider<R, MDL>> =
    (this[model.javaClass] as? List<ExportOperationsProvider<R, MDL>>) ?: emptyList()

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

    fun <ARM : Model<ARM>> createExportOperations(model: ARM, vararg customEnhancers: Enhance<CTX>): Operations<CTX> =
        operationProviders[model].fold(OperationsBuilder(renderingContext)) { builder, provider ->
            builder.apply(provider.provideExportOperations())
        }.build(
            EnableAttributeOperationAwareness(attributeOperationsFactory.createAttributeOperations(model)),
            *customEnhancers
        )

}
