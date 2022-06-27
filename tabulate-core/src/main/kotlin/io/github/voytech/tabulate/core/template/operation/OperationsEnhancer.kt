package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.invoke
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
    private val delegate: ReifiedOperation<CTX, ATTR_CAT, E>,
    attributesOperations: AttributesOperations<CTX>,
) : Operation<CTX, ATTR_CAT, E> {

    private val attributeOperations: List<ReifiedAttributeOperation<CTX, ATTR_CAT, *, E>> =
        attributesOperations.getOperationsBy(delegate.meta)

    private val filteredOperationCache: AttributeClassBasedCache<ATTR_CAT, List<ReifiedAttributeOperation<CTX, ATTR_CAT, *, E>>> =
        AttributeClassBasedCache()

    @Suppress("UNCHECKED_CAST")
    private fun <OP : ReifiedAttributeOperation<CTX, ATTR_CAT, *, E>> E.forEachOperation(
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
    private fun <ATTR : ATTR_CAT> AttributeOperation<CTX, ATTR_CAT, ATTR, E>.renderAttribute(
        renderingContext: CTX,
        context: E,
        clazz: Class<ATTR>,
    ): Boolean {
        return context.getModelAttribute(clazz)?.let {
            invoke(renderingContext, context, it).let { true }
        } ?: false
    }

    private fun <ATTR : ATTR_CAT> AttributeOperation<CTX, ATTR_CAT, ATTR, E>.priority(): Int =
        (this as PrioritizedAttributeOperation<CTX, ATTR_CAT, ATTR, E>).priority

    override operator fun invoke(renderingContext: CTX, context: E) {
        context.withAttributeSetBasedCache {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.forEachOperation(attributeOperations) { attributeOperation ->
                    if (attributeOperation.delegate.priority() >= 0 && !operationRendered) {
                        delegate(renderingContext, context)
                        operationRendered = true
                    }
                    with(attributeOperation.delegate) {
                        renderAttribute(renderingContext, context, attributeOperation.attributeClass())
                    }
                }
            }
            if (!operationRendered) {
                delegate(renderingContext, context)
            }
        }
    }
}

class EnableAttributeOperationAwareness<CTX : RenderingContext>(private val attributesOperations: AttributesOperations<CTX>) :
    Enhance<CTX> {
    override fun <ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>> invoke(op: ReifiedOperation<CTX, ATTR_CAT, E>): Operation<CTX, ATTR_CAT, E> {
        return AttributesAwareExportOperation(op, attributesOperations)
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

    override operator fun invoke(renderingContext: CTX, context: E) {
        layouts?.usingLayout {
            resolveElementBoundaries(context).let { boundaries ->
                delegate(renderingContext, context).also { boundaries?.applyOnLayout() }
                commitBoundaries(context, boundaries)
            }
        }
    }
}

class EnableLayoutsAwareness<CTX : RenderingContext>(private val layouts: Layouts) : Enhance<CTX> {
    override fun <ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>> invoke(op: ReifiedOperation<CTX, ATTR_CAT, E>): Operation<CTX, ATTR_CAT, E> {
        return LayoutAwareOperation(op.delegate, layouts)
    }

}