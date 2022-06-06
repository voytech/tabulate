package io.github.voytech.tabulate.core.template.operation.factories

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributesAwareExportOperation
import io.github.voytech.tabulate.core.template.operation.Enhance
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider

/**
 * Export operations factory that can discover attribute operations.
 * It exposes [Operations] as public API (an interface without attribute-set in operation context).
 * @author Wojciech Mąka
 * @since 0.2.0
 */
abstract class ExportOperationsFactory<CTX : RenderingContext, ARM : Model<ARM>> : OperationsBundleProvider<CTX, ARM>, AttributeOperationsFactory<CTX, ARM>() {

    private val enhancers: MutableList<Enhance<CTX, ARM>> = mutableListOf()

    protected abstract fun provideExportOperations(): OperationsBuilder<CTX, ARM>.() -> Unit

    fun enhanceExportOperations(enhance: Enhance<CTX, ARM>) = enhancers.add(enhance)

    final override fun createExportOperations(): Operations<CTX> =
        OperationsBuilder(getRenderingContextClass(), getModelClass()).apply(provideExportOperations())
            .apply {
                createAttributeOperations().let { attributesOperations ->
                    enhanceOperations { AttributesAwareExportOperation(it, attributesOperations) }
                }
            }.applyCustomEnhancers()
            .build()

    private fun OperationsBuilder<CTX, ARM>.applyCustomEnhancers(): OperationsBuilder<CTX, ARM> = apply {
        enhancers.forEach { enhanceOperations(it) }.also { enhancers.clear() }
    }

    final override fun getRenderingContextClass(): Class<CTX> = getDocumentFormat().provider.renderingContextClass

}
