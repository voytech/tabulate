package io.github.voytech.tabulate.core.operation

import io.github.voytech.tabulate.core.InvokeWithTwoParams
import io.github.voytech.tabulate.core.ReifiedInvocation
import io.github.voytech.tabulate.core.TwoParamsBasedDispatch
import io.github.voytech.tabulate.core.TwoParamsTypeInfo
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.Axis

@Suppress("MemberVisibilityCanBePrivate")
data class RenderingResult(
    val attributes: Map<String, Any> = emptyMap(),
    val status: RenderingStatus
) {
    fun set(status: RenderingStatus): RenderingResult = copy(
        attributes = attributes + ("originalStatus" to this.status),
        status = status,
    )
}

sealed interface RenderingStatus

fun RenderingStatus.asResult(): RenderingResult = RenderingResult(status = this)

open class AxisBoundStatus(val activeAxis: Axis) : RenderingStatus {
    override fun toString(): String = javaClass.simpleName
}

class RenderingSkipped(activeAxis: Axis) : AxisBoundStatus(activeAxis)

class RenderingClipped(activeAxis: Axis) : AxisBoundStatus(activeAxis)

class RenderedPartly(activeAxis: Axis) : AxisBoundStatus(activeAxis)

object Ok : RenderingStatus {
    override fun toString(): String = javaClass.simpleName
}

object Nothing : RenderingStatus {
    override fun toString(): String = javaClass.simpleName

}

object Error : RenderingStatus {
    override fun toString(): String = javaClass.simpleName
}

fun RenderingStatus?.isSkipped(axis: Axis): Boolean = this is RenderingSkipped && activeAxis == axis

fun RenderingStatus?.isClipped(axis: Axis): Boolean = this is RenderingClipped && activeAxis == axis

fun RenderingStatus?.hasOverflown(): Boolean = getAxis()?.let { isSkipped(it) || isClipped(it) } ?: false

fun RenderingResult.hasOverflown(): Boolean = status.hasOverflown()


fun RenderingStatus?.isError(): Boolean = this is Error

fun RenderingStatus?.hasLayoutEffect(): Boolean = !isError() && (this !is RenderingSkipped)

fun RenderingStatus?.getAxis(): Axis? = (this as? AxisBoundStatus)?.activeAxis

fun interface Operation<CTX : RenderingContext, E : AttributedContext> : InvokeWithTwoParams<CTX, E, RenderingResult> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override operator fun invoke(renderingContext: CTX, context: E): RenderingResult

}

typealias ReifiedOperation<CTX, E> = ReifiedInvocation<Operation<CTX, E>, TwoParamsTypeInfo<CTX, E>>

class Operations<CTX : RenderingContext>(private val dispatch: TwoParamsBasedDispatch) {

    operator fun <A : Attribute<*>, E : AttributedContext> invoke(renderingContext: CTX, context: E): RenderingResult =
        (dispatch[renderingContext, context] as? Operation<CTX, E>)?.invoke(renderingContext, context)
            ?: Nothing.asResult()

    fun isEmpty(): Boolean = dispatch.isEmpty()

}

interface Enhance<CTX : RenderingContext> {
    operator fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E>
}

fun interface VoidOperation<CTX : RenderingContext, E : AttributedContext> {
    operator fun invoke(renderingContext: CTX, context: E)
}

class VoidOperationAdapter<CTX : RenderingContext, E : AttributedContext>(val voidOperation: VoidOperation<CTX, E>) :
    Operation<CTX, E> {
    override fun invoke(renderingContext: CTX, context: E): RenderingResult {
        voidOperation(renderingContext, context)
        return Ok.asResult()
    }
}

class OperationsBuilder<CTX : RenderingContext>(val renderingContext: Class<CTX>) {

    private val dispatch: TwoParamsBasedDispatch = TwoParamsBasedDispatch()

    fun <E : AttributedContext> addOperation(clazz: Class<E>, op: Operation<CTX, E>) {
        with(dispatch) {
            op.bind(renderingContext, clazz)
        }
    }

    inline fun <reified E : AttributedContext> operation(op: Operation<CTX, E>) = addOperation(E::class.java, op)

    inline fun <reified E : AttributedContext> operation(op: VoidOperation<CTX, E>) =
        addOperation(E::class.java, VoidOperationAdapter(op))

    @Suppress("UNCHECKED_CAST")
    private fun List<Enhance<CTX>>.applyEnhancers(delegate: ReifiedInvocation<*, *>) =
        fold(delegate as ReifiedOperation<CTX, AttributedContext>) { op, transformer ->
            ReifiedOperation(op.meta, transformer(op))
        }.delegate

    @JvmSynthetic
    internal fun build(vararg enhancers: Enhance<CTX>): Operations<CTX> {
        dispatch.transform { enhancers.toList().applyEnhancers(it) }
        return Operations(dispatch)
    }
}