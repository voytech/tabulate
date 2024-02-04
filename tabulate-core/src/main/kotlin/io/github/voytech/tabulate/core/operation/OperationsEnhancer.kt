package io.github.voytech.tabulate.core.operation

import io.github.voytech.tabulate.core.LayoutApi
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.model.attributes.ClipAttribute
import io.github.voytech.tabulate.core.model.clip.DefaultClippingMode
import io.github.voytech.tabulate.core.model.isNullOrEmpty

/**
 * Operations enhancer bringing attribute-level operations into context of an operation. This allows to delegate each attribute
 * rendering to dedicated operation that can be plugged-in by end user.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
internal class AttributesAwareExportOperation<CTX : RenderingContext, E : AttributedContext>(
    private val reifiedOperation: ReifiedOperation<CTX, E>,
    attributesOperations: AttributesOperations<CTX>,
) : Operation<CTX, E> {

    private val attributeOperations: List<ReifiedAttributeOperation<CTX, *, E>> =
        attributesOperations.getOperationsBy(reifiedOperation.meta)

    private val filteredOperationCache: AttributeClassBasedCache<E, List<ReifiedAttributeOperation<CTX, *, E>>> =
        AttributeClassBasedCache()

    @Suppress("UNCHECKED_CAST")
    private fun <OP : ReifiedAttributeOperation<CTX, *, E>> E.forEachOperation(
        unfiltered: List<OP>, consumer: (operation: OP) -> Boolean,
    ) {
        attributes?.let { attributeList ->
            if (attributeList.isNotEmpty()) {
                if (filteredOperationCache[attributeList].isNullOrEmpty()) {
                    filteredOperationCache[attributeList] = unfiltered.filterTo(mutableListOf()) { consumer(it) }
                } else {
                    filteredOperationCache[attributeList]!!.forEach { consumer(it as OP) }
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

    override operator fun invoke(renderingContext: CTX, context: E): RenderingResult {
        var result = Nothing.asResult()
        context.withAttributeSetBasedCache {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.forEachOperation(attributeOperations) { attributeOperation ->
                    if (attributeOperation.delegate.priority() >= 0 && !operationRendered) {
                        result = reifiedOperation.delegate(renderingContext, context)
                        operationRendered = true
                    }
                    attributeOperation.renderAttribute(renderingContext, context)
                }
            }
            if (!operationRendered) {
                result = reifiedOperation.delegate(renderingContext, context)
            }
        }
        return result
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

sealed class LayoutAwareOperation<CTX : RenderingContext, E : AttributedContext>(
    protected val delegate: Operation<CTX, E>,
    protected val layoutApi: () -> LayoutApi,
) : Operation<CTX, E> {

    protected fun LayoutApi.ensureRenderableBoundingBox(context: E): RenderableBoundingBox? =
        if (context is Renderable<*>) {
            context.initBoundingBox(this)
        } else null

    protected fun LayoutApi.tryApplyResults(context: E) = with(layout) {
        val boundaries = context.boundingBox()
        if (boundaries != null) {
            space.reserveByRectangle(boundaries)
            @Suppress("UNCHECKED_CAST")
            if (context is ApplyLayoutElement<*>) {
                with(context as ApplyLayoutElement<Layout>) {
                    space.applyBoundingBox(boundaries, layout.delegate)
                }
            }
        }
    }

    protected fun Operation<CTX, E>.measureRenderableWhenMissingBounds(
        renderingContext: CTX,
        context: E
    ): RenderingResult =
        if (!context.boundingBox().isDefined()) {
            this(renderingContext, context)
        } else Nothing.asResult()

    protected fun Operations<CTX>.measureRenderableWhenMissingBounds(
        renderingContext: CTX,
        context: E
    ): RenderingResult =
        if (!context.boundingBox().isDefined()) {
            this(renderingContext, context)
        } else Nothing.asResult()

    private fun E.isClippingEnabled(): Boolean =
        getModelAttribute<ClipAttribute>()?.let { clip -> clip.mode == DefaultClippingMode.EDGE } ?: true

    private fun LayoutApi.defaultCrossedAxis(): CrossedAxis =
        CrossedAxis.X.takeIf { layout.properties.orientation == Orientation.HORIZONTAL } ?: CrossedAxis.Y

    protected fun LayoutApi.resolveRenderingStatus(measuringResult: RenderingResult, context: E): RenderingStatus =
        with<Layout, RenderingStatus>(layout) {
            with(space) {
                val maybeCrossedBounds = context.boundingBox()?.let { bbox ->
                    isCrossingBounds(bbox, context.layoutBoundaryToFit())
                }
                val isPartlyRendered = measuringResult.isPartlyRendered()
                return if ((maybeCrossedBounds != null) || isPartlyRendered) {
                    if (context.isClippingEnabled()) {
                        RenderingClipped(maybeCrossedBounds ?: defaultCrossedAxis())
                    } else {
                        RenderingSkipped(maybeCrossedBounds ?: defaultCrossedAxis())
                    }
                } else Ok
            }
        }

}

class LayoutAwareRenderOperation<CTX : RenderingContext, E : AttributedContext>(
    delegate: Operation<CTX, E>,
    layoutApi: () -> LayoutApi, private val measuringOperations: Operations<CTX>,
) : LayoutAwareOperation<CTX, E>(delegate, layoutApi) {

    override operator fun invoke(renderingContext: CTX, context: E): RenderingResult = with(layoutApi()) {
        ensureRenderableBoundingBox(context).let {
            val measuringResult = measuringOperations.measureRenderableWhenMissingBounds(renderingContext, context)
            val status = resolveRenderingStatus(measuringResult, context)
            val intermediateResult = status.merge(measuringResult)
            val renderClippingOrFully: (RenderingResult) -> RenderingResult = { intermediate ->
                intermediate.merge(delegate(renderingContext, context))
            }
            when (status) {
                is Ok -> renderClippingOrFully(intermediateResult)
                is RenderingClipped -> renderClippingOrFully(intermediateResult)
                is RenderingSkipped -> intermediateResult
                else -> error("This OperationResult: $status is not supported on guarded rendering.")
            }.also { tryApplyResults(context) }
        }
    }
}

/**
 * Operations enhancer bringing layout scope into context of an operation. This allows to make use of AttributedModels that
 * are also LayoutElements producing bounding boxes for lay-outing purposes.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class LayoutAwareMeasureOperation<CTX : RenderingContext, E : AttributedContext>(
    delegate: Operation<CTX, E>,
    layoutApi: () -> LayoutApi,
) : LayoutAwareOperation<CTX, E>(delegate, layoutApi) {

    override operator fun invoke(renderingContext: CTX, context: E): RenderingResult = with(layoutApi()) {
        ensureRenderableBoundingBox(context).let {
            val measuringResult = delegate.measureRenderableWhenMissingBounds(renderingContext, context)
            val status = resolveRenderingStatus(measuringResult, context)
            when (status) {
                is RenderingClipped,
                is RenderingSkipped,
                is Ok -> status
                else -> error("This OperationResult: $status is not supported on guarded measuring.")
            }.merge(measuringResult).also { tryApplyResults(context) }
        }
    }
}

class EnableRenderingUsingLayouts<CTX : RenderingContext>(
    private val measuringOperations: Operations<CTX>,
    private val layout: () -> LayoutApi,
) : Enhance<CTX> {
    override fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> =
        LayoutAwareRenderOperation(op.delegate, layout, measuringOperations)
}

class EnableMeasuringForLayouts<CTX : RenderingContext>(
    private val layout: () -> LayoutApi,
) : Enhance<CTX> {
    override fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> =
        LayoutAwareMeasureOperation(op.delegate, layout)
}

