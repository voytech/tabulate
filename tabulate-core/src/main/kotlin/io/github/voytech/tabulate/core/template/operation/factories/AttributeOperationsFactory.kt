package io.github.voytech.tabulate.core.template.operation.factories

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.loadAttributeOperationProviders
import io.github.voytech.tabulate.core.template.operation.AttributeOperationsBuilder
import io.github.voytech.tabulate.core.template.operation.AttributesOperations
import io.github.voytech.tabulate.core.template.spi.AttributeOperationsProvider

/**
 * Export operations factory that can discover attribute operations.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class AttributeOperationsFactory<CTX : RenderingContext>(private val renderingContextType: Class<CTX>) {

    private val attributeOperationsProviders: List<AttributeOperationsProvider<CTX, *>> by lazy {
        loadAttributeOperationProviders(renderingContextType)
    }

    fun <M : Model<M>> createAttributeOperations(model: M): AttributesOperations<CTX> =
        attributeOperationsProviders
            .filter { it.getModelClass() == model.javaClass }
            .fold(AttributeOperationsBuilder(renderingContextType)) { builder, provider ->
                builder.apply(provider.provideAttributeOperations())
            }.build()

}
