package io.github.voytech.tabulate.core.template.operation.factories

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.loadAttributeOperationFactories
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.AttributesOperations
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.spi.AttributeOperationsProvider
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import java.util.*

/**
 * Export operations factory that can discover attribute operations.
 * It exposes [Operations] as public API (an interface without attribute-set in operation context).
 * @author Wojciech Mąka
 * @since 0.2.0
 */
abstract class ExportOperationsFactory<CTX : RenderingContext, ARM : Model<ARM>> : OperationsBundleProvider<CTX, ARM> {

    protected abstract fun provideExportOperations(): OperationsBuilder<CTX, ARM>.() -> Unit

    protected open fun provideAttributeOperations(): Set<AttributeOperation<CTX, ARM, *, *, *>>? = null

    final override fun createExportOperations(): Operations<CTX> = OperationsBuilder(
        getRenderingContextClass(), getModelClass(), createAttributeOperations()
    ).apply(provideExportOperations()).build()

    final override fun createAttributeOperations(): AttributesOperations<CTX, ARM> = registerAttributesOperations()

    final override fun getRenderingContextClass(): Class<CTX> = getDocumentFormat().provider.renderingContextClass

    private fun AttributesOperations<CTX, ARM>.registerAttributesOperations(
        factory: AttributeOperationsProvider<CTX, ARM>?,
    ): AttributesOperations<CTX, ARM> = apply {
        if (factory == this@ExportOperationsFactory) {
            (factory as ExportOperationsFactory<CTX, ARM>).provideAttributeOperations()?.forEach { register(it) }
        } else {
            factory?.createAttributeOperations()?.let { this@apply += it }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun AttributesOperations<CTX, ARM>.registerClientDefinedAttributesOperationsFromProvider():
            AttributesOperations<CTX, ARM> = apply {
        loadAttributeOperationFactories<CTX, ARM>(getRenderingContextClass())
            .filter { it.getModelClass() == getModelClass() }
            .forEach { registerAttributesOperations(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun AttributesOperations<CTX, ARM>.registerClientDefinedAttributesOperations():
            AttributesOperations<CTX, ARM> = apply {
        ServiceLoader.load(AttributeOperation::class.java)
            .filter { getRenderingContextClass().isAssignableFrom(it.typeInfo().renderingContextType) }
            .map { it as AttributeOperation<CTX, ARM, *, *, *> }
            .forEach { register(it) }
    }


    private fun registerAttributesOperations(): AttributesOperations<CTX, ARM> =
        with(AttributesOperations<CTX, ARM>()) {
            registerAttributesOperations(this@ExportOperationsFactory)
            registerClientDefinedAttributesOperationsFromProvider()
            registerClientDefinedAttributesOperations()
        }

}
