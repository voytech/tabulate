package io.github.voytech.tabulate.support.mock

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Size
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.support.TestRenderingContext
import java.util.*

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
        measures.computeIfAbsent(clazz) { mutableListOf() } += MockMeasure(predicate,measure)
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
}

class Spy private constructor() {
    private val visitedOperations: LinkedList<InterceptedContext> = LinkedList()
    internal val measures = MockMeasures()

    internal fun track(
        interceptedOperation: MockOperation,
        context: AttributedContext,
        attribute: Attribute<*>? = null
    ) = visitedOperations.add(InterceptedContext(interceptedOperation, context, attribute))

    fun readHistory(): Iterator<InterceptedContext> =
        LinkedList(visitedOperations).iterator().also {
            visitedOperations.clear()
            operationPriorities.clear()
        }

    companion object {
        val spy: Spy = Spy()
        val operationPriorities: MutableMap<Class<out Attribute<*>>, Int> = mutableMapOf()
    }
}

interface MockOperation

abstract class MockMeasureProvider<E : AttributedContext>(private val spy: Spy = Spy.spy): MockOperation {
    fun tryProvideMeasures(context: E) {
        if (context is Renderable<*>) {
            val maybeSize = spy.measures.run { provideMeasures(context) }
            maybeSize?.let {
                context.boundingBox.width = it.width
                context.boundingBox.height = it.height
            }
        }
    }
}

abstract class MockRenderResultOperation<E : AttributedContext>(
    private val contextClass: Class<E>, private val spy: Spy = Spy.spy
) : MockMeasureProvider<E>(), Operation<TestRenderingContext, E> {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E): RenderingResult {
        tryProvideMeasures(context)
        spy.track(this, context)
        return Ok.asResult()
    }
}

abstract class MockRenderOperation<E : AttributedContext>(
    private val contextClass: Class<E>, private var spy: Spy = Spy.spy
) : MockMeasureProvider<E>(), VoidOperation<TestRenderingContext, E> {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E) {
        tryProvideMeasures(context)
        spy.track(this, context)
    }
}

abstract class MockAttributeRenderOperation<T : Attribute<T>, E : AttributedContext>(
    private val clazz: Class<T>, private val contextClass: Class<E>, private var spy: Spy = Spy.spy
) : AttributeOperation<TestRenderingContext, T, E>, MockOperation {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E, attribute: T) {
        spy.track(this, context, attribute)
    }
}

