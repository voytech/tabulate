package io.github.voytech.tabulate.core.template.operation.factories

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.loadRenderingContextAware
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.AttributesOperationsContainer
import io.github.voytech.tabulate.core.template.operation.Operations
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.core.template.spi.getRenderingContextClass
import java.util.*

/**
 * Export operations factory that can discover attribute operations.
 * It exposes [Operations] as public API (an interface without attribute-set in operation context).
 * @author Wojciech Mąka
 * @since 0.2.0
 */
abstract class ExportOperationsFactory<CTX : RenderingContext, ARM: Model> : ExportOperationsProvider<CTX, ARM> {

    private val attributeOperationsContainer: AttributesOperationsContainer<CTX, ARM> by lazy {
        registerAttributesOperations()
    }

    protected abstract fun provideExportOperations(): OperationsBuilder<CTX, ARM>.() -> Unit

    protected open fun getAttributeOperationsFactory(): AttributeOperationsFactory<CTX, ARM>? = null

    final override fun createExportOperations(): Operations<CTX> = OperationsBuilder(
        getRenderingContextClass(),getAggregateModelClass(), attributeOperationsContainer
    ).apply(provideExportOperations()).build()

    private fun registerAttributesOperations(
        attributeOperationsContainer: AttributesOperationsContainer<CTX, ARM>,
        factory: AttributeOperationsFactory<CTX,ARM>?,
    ): AttributesOperationsContainer<CTX, ARM> {
        return attributeOperationsContainer.apply {
            factory?.createAttributeOperations()?.forEach {
                register(it)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerClientDefinedAttributesOperationsFromProvider( // TODO think if should AttributeOperation be defined per rendering context and aggregate model or only rendering context
        attributeOperationsContainer: AttributesOperationsContainer<CTX, ARM>,
    ): AttributesOperationsContainer<CTX, ARM> = attributeOperationsContainer.apply {
        loadRenderingContextAware<AttributeOperationsFactory<CTX,ARM>, CTX>(getRenderingContextClass())
            .filter { it.getRootModelClass() == getAggregateModelClass() }
            .forEach { registerAttributesOperations(attributeOperationsContainer, it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerClientDefinedAttributesOperations( // TODO think if should AttributeOperation be defined per rendering context and aggregate model or only rendering context
        attributeOperationsContainer: AttributesOperationsContainer<CTX, ARM>
    ): AttributesOperationsContainer<CTX, ARM> = attributeOperationsContainer.apply {
        ServiceLoader.load(AttributeOperation::class.java)
            .filter { getRenderingContextClass().isAssignableFrom(it.typeInfo().renderingContextType) }
            .map { it as AttributeOperation<CTX,ARM , *, *, *> }
            .forEach { attributeOperationsContainer.register(it) }
    }

    private fun registerAttributesOperations(): AttributesOperationsContainer<CTX, ARM> {
        return AttributesOperationsContainer<CTX, ARM>().let {
            registerAttributesOperations(it, getAttributeOperationsFactory())
            registerClientDefinedAttributesOperationsFromProvider(it)
            registerClientDefinedAttributesOperations(it)
        }
    }

}
