package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.template.spi.getRenderingContextClass
import java.util.*

/**
 * Export operations factory that can discover attribute operations.
 * It exposes [Operations] as public API (an interface without attribute-set in operation context).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class ExportOperationsFactory<CTX : RenderingContext> : ExportOperationsProvider<CTX> {

    private val attributeOperationsContainer: AttributesOperationsContainer<CTX> by lazy {
        registerAttributesOperations()
    }

    protected abstract fun provideExportOperations(): OperationsBuilder<CTX>.() -> Unit

    abstract override fun createOutputBindings(): List<OutputBinding<CTX, *>>

    protected open fun getAttributeOperationsFactory(): AttributeOperationsFactory<CTX>? = null

    final override fun createExportOperations(): Operations<CTX> = OperationsBuilder<CTX>()
        .apply(provideExportOperations()).build(
            getTabulationFormat().provider.renderingContextClass,
            attributeOperationsContainer
        )

    private fun registerAttributesOperations(
        attributeOperationsContainer: AttributesOperationsContainer<CTX>,
        factory: AttributeOperationsFactory<CTX>?,
    ): AttributesOperationsContainer<CTX> {
        return attributeOperationsContainer.apply {
            factory?.let { this.registerAttributesOperations(it) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerClientDefinedAttributesOperations(
        attributeOperationsContainer: AttributesOperationsContainer<CTX>,
    ): AttributesOperationsContainer<CTX> {
        ServiceLoader.load(AttributeRenderOperationsProvider::class.java)
            .filter { getRenderingContextClass().isAssignableFrom(it.getContextClass()) }
            .map { it as AttributeRenderOperationsProvider<CTX> }
            .forEach {
                registerAttributesOperations(attributeOperationsContainer, it.getAttributeOperationsFactory())
            }
        return attributeOperationsContainer
    }

    private fun registerAttributesOperations(): AttributesOperationsContainer<CTX> {
        return AttributesOperationsContainer<CTX>().let {
            registerAttributesOperations(it, getAttributeOperationsFactory())
            registerClientDefinedAttributesOperations(it)
        }
    }

}
