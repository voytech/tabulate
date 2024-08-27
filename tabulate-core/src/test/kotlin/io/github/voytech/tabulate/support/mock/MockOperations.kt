package io.github.voytech.tabulate.support.mock

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.round
import io.github.voytech.tabulate.support.TestRenderingContext
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

data class InterceptedContext(
    val operation: MockOperation,
    val context: AttributedEntity,
    val attribute: Attribute<*>? = null
)

data class MockMeasure<R : RenderableEntity<*>>(val predicate: (R) -> Boolean, val measure: (RenderableBoundingBox) -> Size)

class MockMeasures {
    private val measures: MutableMap<Class<out RenderableEntity<*>>, MutableList<MockMeasure<*>>> = mutableMapOf()
    inline fun <reified R : RenderableEntity<*>> register(
        noinline predicate: (R) -> Boolean, noinline measure: (RenderableBoundingBox) -> Size
    ) {
        registerTyped(R::class.java, predicate, measure)
    }

    fun <R : RenderableEntity<*>> registerTyped(
        clazz: Class<out R>, predicate: (R) -> Boolean, measure: (RenderableBoundingBox) -> Size
    ) {
        measures.computeIfAbsent(clazz) { mutableListOf() } += MockMeasure(predicate, measure)
    }

    @Suppress("UNCHECKED_CAST")
    fun MockOperation.provideMeasures(renderable: RenderableEntity<*>): Size? {
        return measures[renderable::class.java]?.let { all ->
            all.find {
                it as MockMeasure<RenderableEntity<*>>
                it.predicate(renderable)
            }?.measure?.let { it(renderable.boundingBox) }
        }
    }

    fun clear() {
        measures.clear()
    }
}

class Spy private constructor() {
    private val visitedOperations: LinkedList<InterceptedContext> = LinkedList()
    internal val measures = MockMeasures()

    // When set to true operations that have effect in actual rendering renderable object in viewport will be tracked.
    internal var trackRendering = true

    // When set to false operations that are used to measure renderable (when no size provided) will be skipped.
    internal var trackMeasuring = true
    var documentWidth: Width? = null
    var documentHeight: Height? = null

    internal fun track(
        interceptedOperation: MockOperation,
        context: AttributedEntity,
        attribute: Attribute<*>? = null
    ) = visitedOperations.add(InterceptedContext(interceptedOperation, context, attribute))

    fun readHistory(): Iterator<InterceptedContext> =
        LinkedList(visitedOperations).iterator().also {
            visitedOperations.clear()
            operationPriorities.clear()
            measures.clear()
            documentHeight = null
            documentWidth = null
        }

    companion object {
        val spy: Spy = Spy()
        val operationPriorities: MutableMap<Class<out Attribute<*>>, Int> = mutableMapOf()
    }
}

interface MockOperation

abstract class MockMeasureProvider<E : AttributedEntity>(private val spy: Spy = Spy.spy) : MockOperation {
    fun tryProvideMeasures(context: E) {
        if (context is RenderableEntity<*>) {
            if (context.boundingBox.isDefined()) return
            val maybeSize = spy.measures.run { provideMeasures(context) }
            maybeSize?.let {
                context.boundingBox.width = it.width
                context.boundingBox.height = it.height
            }
        }
    }
}

abstract class MockRenderResultOperation<E : AttributedEntity>(
    private val contextClass: Class<E>, private val isMeasuringOp: Boolean = false,
    private val measure: Boolean = true, private val spy: Spy = Spy.spy
) : MockMeasureProvider<E>(), Operation<TestRenderingContext, E> {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E): RenderingResult {
        if (measure) tryProvideMeasures(context)
        if (isMeasuringOp && spy.trackMeasuring ||
            !isMeasuringOp && spy.trackRendering
        ) {
            spy.track(this, context)
        }
        return Ok.asResult()
    }
}

abstract class MockRenderOperation<E : AttributedEntity>(
    private val contextClass: Class<E>,
    val isMeasuringOperation: Boolean = false,
    private val measure: Boolean = true,
    private val spy: Spy = Spy.spy
) : MockMeasureProvider<E>(), VoidOperation<TestRenderingContext, E> {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E) {
        if (measure) tryProvideMeasures(context)
        if (isMeasuringOperation && spy.trackMeasuring ||
            !isMeasuringOperation && spy.trackRendering
        ) {
            spy.track(this, context)
        }
    }
}

open class MockAttributeRenderOperation<T : Attribute<T>, E : AttributedEntity>(
    private val clazz: Class<T>,
    private val contextClass: Class<E>,
    private val isMeasuringOpr: Boolean = false,
    private val spy: Spy = Spy.spy
) : AttributeOperation<TestRenderingContext, T, E>, MockOperation {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E, attribute: T) {
        spy.track(this, context, attribute)
    }

    companion object {
        inline operator fun <reified E : AttributedEntity, reified T : Attribute<T>> invoke() =
            MockAttributeRenderOperation(T::class.java, E::class.java)
    }
}

fun InterceptedContext.assertIsMeasuringOperation(): Boolean =
    (operation as? MockRenderOperation<*>)?.isMeasuringOperation ?: false

fun InterceptedContext.assertIsRenderingOperation(): Boolean =
    (operation as? MockRenderOperation<*>)?.isMeasuringOperation?.not() ?: true

inline fun <reified R: AttributedEntity> InterceptedContext.assertContextClass() =
    context::class.java == R::class.java

fun InterceptedContext.assertIsRenderable() {
    assertTrue { context is RenderableEntity<*> }
}

fun InterceptedContext.assertBoundingBox(fractionPrecision: Int, boundingBox: BoundingRectangle) {
    assertIsRenderable()
    val renderable = context as RenderableEntity<*>
    val renderableBoundingBox = renderable.boundingBox
    assertEquals(
        boundingBox.leftTop.x.value.round(fractionPrecision),
        renderableBoundingBox.absoluteX.value.round(fractionPrecision)
    )
    assertEquals(
        boundingBox.leftTop.y.value.round(fractionPrecision),
        renderableBoundingBox.absoluteY.value.round(fractionPrecision)
    )
    assertEquals(
        boundingBox.rightBottom.x.value.round(fractionPrecision),
        (renderableBoundingBox.absoluteX + (renderableBoundingBox.width ?: 0f.asWidth())).value.round(
            fractionPrecision
        )
    )
    assertEquals(
        boundingBox.rightBottom.y.value.round(fractionPrecision),
        (renderableBoundingBox.absoluteY + (renderableBoundingBox.height ?: 0f.asHeight())).value.round(
            fractionPrecision
        )
    )
}

inline fun <reified C: AttributedEntity> InterceptedContext.assertPredicate(predicate: (C) -> Boolean): Boolean {
    assertContextClass<C>()
    return predicate(context as C).also { assertTrue { it } }
}

fun Iterator<InterceptedContext>.assertOperationAssertionsInOrder(vararg predicates: (InterceptedContext) -> Boolean) {
    for (predicate in predicates) {
        assertTrue { hasNext() }
        val current = next()
        assertTrue { predicate(current) }
    }
    assertFalse { hasNext() }
}

fun Iterator<InterceptedContext>.assertAttributedContextsAppearanceInOrder(vararg classes: KClass<out AttributedEntity>) {
    for (contextClass in classes) {
        println(contextClass)
        assertTrue { hasNext() }
        val current = next()
        assertTrue { current.context::class == contextClass }
    }
}

fun Iterator<InterceptedContext>.assertRenderableBoundingBoxesInOrder(
    fractionPrecision: Int,
    vararg boundingBoxes: BoundingRectangle
) {
    boundingBoxes.forEach { boundingBox ->
        assertTrue { hasNext() }
        val current = next()
        current.assertBoundingBox(fractionPrecision, boundingBox)
    }
    assertFalse { hasNext() }
}
