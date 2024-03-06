package io.github.voytech.tabulate.support.mock

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.round
import io.github.voytech.tabulate.support.TestRenderingContext
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

data class InterceptedContext(
    val operation: MockOperation,
    val context: AttributedContext,
    val attribute: Attribute<*>? = null
)

data class MockMeasure<R : Renderable<*>>(val predicate: (R) -> Boolean, val measure: (RenderableBoundingBox) -> Size)

class MockMeasures {
    private val measures: MutableMap<Class<out Renderable<*>>, MutableList<MockMeasure<*>>> = mutableMapOf()
    inline fun <reified R : Renderable<*>> register(
        noinline predicate: (R) -> Boolean, noinline measure: (RenderableBoundingBox) -> Size
    ) {
        registerTyped(R::class.java, predicate, measure)
    }

    fun <R : Renderable<*>> registerTyped(
        clazz: Class<out R>, predicate: (R) -> Boolean, measure: (RenderableBoundingBox) -> Size
    ) {
        measures.computeIfAbsent(clazz) { mutableListOf() } += MockMeasure(predicate, measure)
    }

    @Suppress("UNCHECKED_CAST")
    fun MockOperation.provideMeasures(renderable: Renderable<*>): Size? {
        return measures[renderable::class.java]?.let { all ->
            all.find {
                it as MockMeasure<Renderable<*>>
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

    internal var trackRendering = true
    internal var trackMeasuring = true
    var documentWidth: Width? = null
    var documentHeight: Height? = null

    internal fun track(
        interceptedOperation: MockOperation,
        context: AttributedContext,
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

abstract class MockMeasureProvider<E : AttributedContext>(private val spy: Spy = Spy.spy) : MockOperation {
    fun tryProvideMeasures(context: E) {
        if (context is Renderable<*>) {
            if (context.boundingBox.isDefined()) return
            val maybeSize = spy.measures.run { provideMeasures(context) }
            maybeSize?.let {
                context.boundingBox.width = it.width
                context.boundingBox.height = it.height
            }
        }
    }
}

abstract class MockRenderResultOperation<E : AttributedContext>(
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

abstract class MockRenderOperation<E : AttributedContext>(
    private val contextClass: Class<E>,
    private val isMeasuringOp: Boolean = false,
    private val measure: Boolean = true,
    private val spy: Spy = Spy.spy
) : MockMeasureProvider<E>(), VoidOperation<TestRenderingContext, E> {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E) {
        if (measure) tryProvideMeasures(context)
        if (isMeasuringOp && spy.trackMeasuring ||
            !isMeasuringOp && spy.trackRendering
        ) {
            spy.track(this, context)
        }
    }
}

open class MockAttributeRenderOperation<T : Attribute<T>, E : AttributedContext>(
    private val clazz: Class<T>,
    private val contextClass: Class<E>,
    private val isMeasuringOpr: Boolean = false,
    private val spy: Spy = Spy.spy
) : AttributeOperation<TestRenderingContext, T, E>, MockOperation {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E, attribute: T) {
        spy.track(this, context, attribute)
    }

    companion object {
        inline operator fun <reified E : AttributedContext, reified T : Attribute<T>> invoke() =
            MockAttributeRenderOperation(T::class.java, E::class.java)
    }
}


fun Iterator<InterceptedContext>.assertAttributedContextsAppearanceInOrder(vararg classes: KClass<out AttributedContext>) {
    for (contextClass in classes) {
        println(contextClass)
        assertTrue { hasNext() }
        val current = next()
        assertTrue { current.context::class == contextClass }
    }
}

fun Iterator<InterceptedContext>.assertRenderableBoundingBoxesInOrder(fractionPrecision: Int,vararg boundingBoxes: BoundingRectangle) {
    boundingBoxes.forEach { boundingBox ->
        assertTrue { hasNext() }

        val current = next()
        val renderableBoundingBox = (current.context as? Renderable<*>)?.boundingBox

        assertNotNull(renderableBoundingBox)

        assertEquals(boundingBox.leftTop.x.value.round(fractionPrecision), renderableBoundingBox.absoluteX.value.round(fractionPrecision))
        assertEquals(boundingBox.leftTop.y.value.round(fractionPrecision), renderableBoundingBox.absoluteY.value.round(fractionPrecision))
        assertEquals(
            boundingBox.rightBottom.x.value.round(fractionPrecision),
            (renderableBoundingBox.absoluteX + (renderableBoundingBox.width ?: 0f.asWidth())).value.round(fractionPrecision)
        )
        assertEquals(
            boundingBox.rightBottom.y.value.round(fractionPrecision),
            (renderableBoundingBox.absoluteY + (renderableBoundingBox.height ?: 0f.asHeight())).value.round(fractionPrecision)
        )
    }
}
