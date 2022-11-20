package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.invoke
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.isNullOrEmpty
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.*

/**
 * Operations enhancer bringing attribute-level operations into context of an operation. This allows to delegate each attribute
 * rendering to dedicated operation that can be plugged-in by end user.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
internal class AttributesAwareExportOperation<CTX : RenderingContext, E : AttributedContext>(
    private val delegate: ReifiedOperation<CTX, E>,
    attributesOperations: AttributesOperations<CTX>,
) : Operation<CTX, E> {

    private val attributeOperations: List<ReifiedAttributeOperation<CTX, *, E>> =
        attributesOperations.getOperationsBy(delegate.meta)

    private val filteredOperationCache: AttributeClassBasedCache<E, List<ReifiedAttributeOperation<CTX, *, E>>> =
        AttributeClassBasedCache()

    @Suppress("UNCHECKED_CAST")
    private fun <OP : ReifiedAttributeOperation<CTX, *, E>> E.forEachOperation(
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
    private fun <A : Attribute<A>> ReifiedAttributeOperation<CTX, *, E>.renderAttribute(
        renderingContext: CTX, context: E,
    ): Boolean {
        val clazz = meta.t3 as Class<A>
        return context.getModelAttribute(clazz)?.let {
            (delegate as AttributeOperation<CTX, A, E>).invoke(renderingContext, context, it).let { true }
        } ?: false
    }

    private fun <A : Attribute<A>> AttributeOperation<CTX, A, E>.priority(): Int =
        (this as PrioritizedAttributeOperation<CTX, A, E>).priority

    override operator fun invoke(renderingContext: CTX, context: E) {
        context.withAttributeSetBasedCache {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.forEachOperation(attributeOperations) { attributeOperation ->
                    if (attributeOperation.delegate.priority() >= 0 && !operationRendered) {
                        delegate(renderingContext, context)
                        operationRendered = true
                    }
                    attributeOperation.renderAttribute(renderingContext, context)
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
    override fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> {
        return AttributesAwareExportOperation(op, attributesOperations)
    }
}

/**
 * Operations enhancer bringing layout scope into context of an operation. This allows to make use of AttributedModels that
 * are also LayoutElements producing bounding boxes for lay-outing purposes.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class LayoutAwareOperation<CTX : RenderingContext, E : AttributedContext>(
    private val delegate: Operation<CTX, E>,
    private val checkOverflows: Boolean = true,
    private val layout: () -> Layout,
) : Operation<CTX, E> {

    private fun <E : RenderableContext> Layout.mergeAttributeElementBoundingBox(
        context: E, boundingBox: LayoutElementBoundingBox,
    ): LayoutElementBoundingBox =
        context.attributes?.attributeSet?.asSequence()?.let { attributes ->
            attributes.filterIsInstance<BoundingBoxModifier>()
                .fold(boundingBox) { bbox, next ->
                    bbox + with(next) { alter(bbox) }
                }
        } ?: run { context.boundingBox }


    private fun <E : AttributedContext> Layout.resolveElementBoundingBox(context: E): LayoutElementBoundingBox? =
        if (context is RenderableContext) {
            with(context) {
                initBoundingBox { bbox -> mergeAttributeElementBoundingBox(context, bbox) }
            }
        } else null

    private fun <E : AttributedContext> Layout.commitBoundaries(
        context: E, boundaries: LayoutElementBoundingBox? = null,
    ) {
        if (boundaries != null) {
            // TODO !!!!!!!!!!!!!!!!!!!!!!!!!
            // TODO should not need LayoutElementApply. Layout itself should use boundaries and apply them through its policy.
            // TODO !!!!!!!!!!!!!!!!!!!!!!!!!
            if (context is LayoutElementApply) with(context) { applyBoundingBox(boundaries) }
        }
    }

    override operator fun invoke(renderingContext: CTX, context: E) {
        with(layout()) {
            resolveElementBoundingBox(context).let { bbox ->
                ifEnabled { bbox.checkOverflow() }?.let {
                    context.setResult(OverflowResult(it))
                } ?: run {
                    delegate(renderingContext, context).also {
                        bbox?.applyOnLayout()
                    }
                    commitBoundaries(context, bbox)
                    context.setResult(Success)
                }
            }
        }
    }

    private fun ifEnabled(provider: () -> Overflow?): Overflow? =
        if (checkOverflows) provider() else null

}

class EnableLayoutsAwareness<CTX : RenderingContext>(
    private val checkOverflows: Boolean = true, private val layout: () -> Layout,
) : Enhance<CTX> {
    override fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> {
        return LayoutAwareOperation(op.delegate, checkOverflows, layout)
    }
}

class SkipRedundantMeasurements<CTX : RenderingContext> : Enhance<CTX> {
    override fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> =
        Operation { renderingContext, context ->
            if (!context.boundingBox().isDefined()) {
                op(renderingContext, context)
            }
        }
}

class JoinOperations<CTX : RenderingContext>(
    private val other: Operations<CTX>,
    private val predicate: (AttributedContext) -> Boolean,
) : Enhance<CTX> {
    override fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> =
        Operation { renderingContext, context ->
            if (predicate(context)) {
                other(renderingContext, context)
            }
            op(renderingContext, context)
        }
}