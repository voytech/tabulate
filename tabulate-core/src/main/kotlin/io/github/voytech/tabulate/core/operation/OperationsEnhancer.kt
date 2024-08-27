package io.github.voytech.tabulate.core.operation

import io.github.voytech.tabulate.core.RelatedLayouts
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.ClipAttribute
import io.github.voytech.tabulate.core.model.clip.ClippingMode

/**
 * Operations enhancer bringing attribute-level operations into context of an operation. This allows to delegate each attribute
 * rendering to dedicated operation that can be plugged-in by end user.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
internal class AttributesAwareExportOperation<CTX : RenderingContext, E : AttributedEntity>(
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
    override fun <E : AttributedEntity> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> {
        return AttributesAwareExportOperation(op, attributesOperations)
    }
}

/**
 * Operations enhancer bringing layout scope into context of an operation. This allows to make use of AttributedModels that
 * are also LayoutElements producing bounding boxes for lay-outing purposes.
 * @author Wojciech Mąka
 * @since 0.2.0
 */

sealed class LayoutAwareOperation<CTX : RenderingContext, E : AttributedEntity>(
    protected val delegate: Operation<CTX, E>,
    protected val layoutProvider: () -> RelatedLayouts,
) : Operation<CTX, E> {

    protected fun RelatedLayouts.ensureRenderableBoundingBox(context: E): RenderableBoundingBox? =
        if (context is RenderableEntity<*>) {
            context.initBoundingBox(this)
        } else null


    protected fun RelatedLayouts.tryApplyResults(context: E, status: RenderingStatus) {
        context.boundingBox()?.let { boundaries ->
            // before applying potentially recalculated bbox onto region, first ensure it does not exceed outer layout bounds.
            layout.allocateRectangle(boundaries.revalidateSize())
            context.asLayoutElement<Layout>()?.run {
                layout.absorbRenderableBoundingBox(boundaries, status)
            }
        }
    }

    private fun E.checkIfSkippedOrClipped(directResult: RenderingResult): RenderingResult =
        when (directResult.status) {
            is RenderedPartly -> {
                val axis = directResult.status.activeAxis
                if (isClippingEnabled()) {
                    directResult.set(RenderingClipped(axis))
                } else directResult.set(RenderingSkipped(axis))
            }

            else -> directResult
        }

    protected fun Operation<CTX, E>.measureRenderableWhenMissingBounds(
        renderingContext: CTX,
        context: E
    ): RenderingResult =
        if (!context.boundingBox().isDefined()) {
            context.checkIfSkippedOrClipped(invoke(renderingContext, context))
        } else Nothing.asResult()

    protected fun Operations<CTX>.measureRenderableWhenMissingBounds(
        renderingContext: CTX,
        context: E
    ): RenderingResult =
        if (!context.boundingBox().isDefined()) {
            context.checkIfSkippedOrClipped(invoke(renderingContext, context))
        } else Nothing.asResult()

    private fun E.isClippingEnabled(): Boolean =
        getModelAttribute<ClipAttribute>()?.let { clip -> clip.mode == ClippingMode.CLIP }
            ?: true //TODO get default value from global export configurations.

    private fun RelatedLayouts.defaultCrossedAxis(): Axis =
        Axis.X.takeIf { layout.properties.orientation == Orientation.HORIZONTAL }
            ?: Axis.Y //TODO get default value from global export configurations.

    protected fun RelatedLayouts.checkOverflowStatus(context: E): RenderingStatus {
        val maybeCrossedBounds = context.boundingBox()?.let { bbox ->
            layout.isCrossingBounds(bbox, context.layoutBoundaryToFit())
        }
        return if (maybeCrossedBounds != null) {
            if (context.isClippingEnabled()) {
                RenderingClipped(maybeCrossedBounds)
            } else {
                RenderingSkipped(maybeCrossedBounds)
            }
        } else Ok
    }

}

class LayoutAwareRenderOperation<CTX : RenderingContext, E : AttributedEntity>(
    delegate: Operation<CTX, E>,
    layoutProvider: () -> RelatedLayouts, private val measuringOperations: Operations<CTX>,
) : LayoutAwareOperation<CTX, E>(delegate, layoutProvider) {

    override operator fun invoke(renderingContext: CTX, context: E): RenderingResult = with(layoutProvider()) {
        ensureRenderableBoundingBox(context).let {
            val result = measuringOperations.measureRenderableWhenMissingBounds(renderingContext, context)
            val status = checkOverflowStatus(context)
            traceSection("$context", "Overflow status: [${status}] ", "Measure result: [${result.status}]")
            val intermediateResult = result.set(status)
            val renderClippingOrFully: (RenderingResult) -> RenderingResult = { intermediate ->
                delegate(renderingContext, context).set(intermediate.status)
            }
            when (intermediateResult.status) {
                is Ok -> renderClippingOrFully(intermediateResult)
                is RenderingClipped -> renderClippingOrFully(intermediateResult)
                is RenderingSkipped -> intermediateResult
                else -> error("This OperationResult: ${intermediateResult.status} is not supported on guarded rendering.")
            }.also { tryApplyResults(context, it.status) }
        }
    }
}

/**
 * Operations enhancer bringing layout scope into context of an operation. This allows to make use of AttributedModels that
 * are also LayoutElements producing bounding boxes for lay-outing purposes.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class LayoutAwareMeasureOperation<CTX : RenderingContext, E : AttributedEntity>(
    delegate: Operation<CTX, E>,
    layoutProvider: () -> RelatedLayouts,
) : LayoutAwareOperation<CTX, E>(delegate, layoutProvider) {

    override operator fun invoke(renderingContext: CTX, context: E): RenderingResult = with(layoutProvider()) {
        ensureRenderableBoundingBox(context).let {
            val result = delegate.measureRenderableWhenMissingBounds(renderingContext, context)
            val status = checkOverflowStatus(context)
            traceSection("$context", "Overflow status: [${status}] ", "Measure result: [${result.status}]")
            when (status) {
                is RenderingClipped,
                is RenderingSkipped -> status

                is Ok -> result.status
                else -> error("This OperationResult: $status is not supported on guarded measuring.")
            }.let { newStatus -> result.set(newStatus) }.also { tryApplyResults(context, it.status) }
        }
    }
}

class EnableRenderingUsingLayouts<CTX : RenderingContext>(
    private val measuringOperations: Operations<CTX>,
    private val layoutProvider: () -> RelatedLayouts,
) : Enhance<CTX> {
    override fun <E : AttributedEntity> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> =
        LayoutAwareRenderOperation(op.delegate, layoutProvider, measuringOperations)
}

class EnableMeasuringForLayouts<CTX : RenderingContext>(
    private val layoutProvider: () -> RelatedLayouts,
) : Enhance<CTX> {
    override fun <E : AttributedEntity> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E> =
        LayoutAwareMeasureOperation(op.delegate, layoutProvider)
}

