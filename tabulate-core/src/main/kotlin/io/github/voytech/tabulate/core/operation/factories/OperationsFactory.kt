package io.github.voytech.tabulate.core.operation.factories

import io.github.voytech.tabulate.core.DocumentFormat
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.loadExportOperationProviders
import io.github.voytech.tabulate.core.loadMeasureOperationProviders
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.operation.EnableAttributeOperationAwareness
import io.github.voytech.tabulate.core.operation.Enhance
import io.github.voytech.tabulate.core.operation.Operations
import io.github.voytech.tabulate.core.operation.OperationsBuilder
import io.github.voytech.tabulate.core.spi.ExportOperationsProvider
import io.github.voytech.tabulate.core.spi.MeasureOperationsProvider
import io.github.voytech.tabulate.core.spi.getRenderingContextClass

typealias DiscoveredExportOperationProviders<R> = Map<Class<out AbstractModel>, List<ExportOperationsProvider<R, *>>>

typealias DiscoveredMeasureOperationProviders<R> = Map<Class<out AbstractModel>, List<MeasureOperationsProvider<R, *>>>


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

    fun <M : AbstractModel> createExportOperations(model: M, vararg customEnhancers: Enhance<CTX>): Operations<CTX> =
        (exportOperationProviders[model.javaClass]?: emptyList()).fold(OperationsBuilder(renderingContext)) { builder, provider ->
            builder.apply(provider.provideExportOperations())
        }.build(
            EnableAttributeOperationAwareness(attributeOperationsFactory.createAttributeOperations(model)),
            *customEnhancers
        )

    fun <M : AbstractModel> createMeasureOperations(model: M, vararg customEnhancers: Enhance<CTX>): Operations<CTX> =
        (measureOperationProviders[model.javaClass]?: emptyList()).fold(OperationsBuilder(renderingContext)) { builder, provider ->
            builder.apply(provider.provideMeasureOperations())
        }.build(*customEnhancers)

}
