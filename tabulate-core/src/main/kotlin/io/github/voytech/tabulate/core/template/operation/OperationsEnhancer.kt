package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.model.isNullOrEmpty
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.*

/**
 * Operations enhancer bringing attribute-level operations into context of an operation. This allows to delegate each attribute
 * rendering to dedicated operation that can be plugged-in by end user.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
internal class AttributesAwareExportOperation<CTX : RenderingContext, M : Model<M>, ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>>(
    delegate: ReifiedOperation<CTX, ATTR_CAT, E, M>,
    attributesOperations: AttributesOperations<CTX, M>,
) : ReifiedOperation<CTX, ATTR_CAT, E, M>(delegate, delegate.typeInfo) {

    private val attributeOperations: List<AttributeOperation<CTX, M, ATTR_CAT, *, E>> =
        attributesOperations.getOperationsBy(delegate.typeInfo)

    private val filteredOperationCache: AttributeClassBasedCache<ATTR_CAT, List<AttributeOperation<CTX, M, ATTR_CAT, *, E>>> =
        AttributeClassBasedCache()

    @Suppress("UNCHECKED_CAST")
    private fun <OP : AttributeOperation<CTX, M, ATTR_CAT, *, E>> E.forEachOperation(
        unfiltered: List<OP>, consumer: (operation: OP) -> Boolean,
    ) {
        attributes?.let { _attributes ->
            if (_attributes.isNotEmpty()) {
                if (filteredOperationCache[_attributes].isNullOrEmpty()) {
                    filteredOperationCache[_attributes] = unfiltered.filterTo(mutableListOf()) { consumer(it) }
                } else {
                    filteredOperationCache[_attributes]!!.forEach { consumer(it as OP) }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <ATTR : ATTR_CAT> AttributeOperation<CTX, M, ATTR_CAT, ATTR, E>.renderAttribute(
        renderingContext: CTX,
        context: E,
        clazz: Class<out ATTR_CAT>,
    ): Boolean {
        return context.getModelAttribute(clazz)?.let {
            renderAttribute(renderingContext, context, it as ATTR).let { true }
        } ?: false
    }

    override fun render(renderingContext: CTX, context: E) {
        context.withAttributeSetBasedCache {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.forEachOperation(attributeOperations) { attributeOperation ->
                    if (attributeOperation.priority() >= 0 && !operationRendered) {
                        delegate.render(renderingContext, context)
                        operationRendered = true
                    }
                    with(attributeOperation) {
                        renderAttribute(renderingContext, context, attributeClass())
                    }
                }
            }
            if (!operationRendered) {
                delegate.render(renderingContext, context)
            }
        }
    }
}

/**
 * Operations enhancer bringing layout scope into context of an operation. This allows to make use of AttributedModels that
 * are also LayoutElements producing bounding boxes for lay-outing purposes.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class LayoutAwareOperation<CTX : RenderingContext, A : Attribute<*>, E : AttributedContext<A>>(
    private val delegate: Operation<CTX, A, E>, private val layouts: Layouts? = null,
) : Operation<CTX, A, E> {

    private fun <A : Attribute<*>, E : AttributedContext<A>> Layout.resolveAttributeElementBoundaries(context: E): LayoutElementBoundaries? {
        context.attributes?.attributeSet?.forEach {
            if (it is LayoutElement) with(it) {
                computeBoundaries().mergeInto(context)
            }
        }
        return context.boundaries()
    }

    private fun <A : Attribute<*>, E : AttributedContext<A>> Layout.resolveElementBoundaries(context: E): LayoutElementBoundaries? {
        if (context is LayoutElement) with(context) {
            computeBoundaries().mergeInto(context)
            resolveAttributeElementBoundaries(context)
        }
        return context.boundaries()
    }

    private fun <A : Attribute<*>, E : AttributedContext<A>> Layout.commitBoundaries(
        context: E, boundaries: LayoutElementBoundaries? = null,
    ) {
        if (boundaries != null) {
            if (context is LayoutElementApply) with(context) { applyBoundaries(boundaries) }
            context.dropBoundaries()
        }
    }

    override fun render(renderingContext: CTX, context: E) {
        layouts?.usingLayout {
            resolveElementBoundaries(context).let { boundaries ->
                delegate.render(renderingContext, context).also { boundaries?.applyOnLayout() }
                commitBoundaries(context, boundaries)
            }
        }
    }
}