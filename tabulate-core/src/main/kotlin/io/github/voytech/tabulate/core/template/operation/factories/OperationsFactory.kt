package io.github.voytech.tabulate.core.template.operation.factories

import io.github.voytech.tabulate.core.model.UnconstrainedModel
import io.github.voytech.tabulate.core.template.DocumentFormat
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.loadExportOperationProviders
import io.github.voytech.tabulate.core.template.loadMeasureOperationProviders
import io.github.voytech.tabulate.core.template.operation.EnableAttributeOperationAwareness
import io.github.voytech.tabulate.core.template.operation.Enhance
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.core.template.spi.MeasureOperationsProvider
import io.github.voytech.tabulate.core.template.spi.getRenderingContextClass

typealias DiscoveredExportOperationProviders<R> = Map<Class<out UnconstrainedModel<*>>, List<ExportOperationsProvider<R, *>>>

typealias DiscoveredMeasureOperationProviders<R> = Map<Class<out UnconstrainedModel<*>>, List<MeasureOperationsProvider<R, *>>>


private fun <R : RenderingContext> DiscoveredExportOperationProviders<R>.getRenderingContextClass(): Class<R> =
    values.first().first().getRenderingContextClass()


/**
 * Export operations factory that can discover attribute operations.
 * It exposes [Operations] as public API (an interface without attribute-set in operation context).
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class OperationsFactory<CTX : RenderingContext>(private val documentFormat: DocumentFormat) {

    private val exportOperationProviders: DiscoveredExportOperationProviders<CTX> by lazy {
        loadExportOperationProviders(documentFormat)
    }

    private val measureOperationProviders: DiscoveredMeasureOperationProviders<CTX> by lazy {
        loadMeasureOperationProviders(documentFormat)
    }

    internal val renderingContext: Class<CTX> by lazy {
        exportOperationProviders.getRenderingContextClass()
    }

    private val attributeOperationsFactory: AttributeOperationsFactory<CTX> by lazy {
        AttributeOperationsFactory(renderingContext)
    }

    fun <M : UnconstrainedModel<M>> createExportOperations(model: M, vararg customEnhancers: Enhance<CTX>): Operations<CTX> =
        (exportOperationProviders[model.javaClass]?: emptyList()).fold(OperationsBuilder(renderingContext)) { builder, provider ->
            builder.apply(provider.provideExportOperations())
        }.build(
            EnableAttributeOperationAwareness(attributeOperationsFactory.createAttributeOperations(model)),
            *customEnhancers
        )

    fun <M : UnconstrainedModel<M>> createMeasureOperations(model: M, vararg customEnhancers: Enhance<CTX>): Operations<CTX> =
        (measureOperationProviders[model.javaClass]?: emptyList()).fold(OperationsBuilder(renderingContext)) { builder, provider ->
            builder.apply(provider.provideMeasureOperations())
        }.build(*customEnhancers)

}
