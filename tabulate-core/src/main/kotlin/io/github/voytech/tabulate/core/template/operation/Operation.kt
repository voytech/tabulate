package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.InvokeWithTwoParams
import io.github.voytech.tabulate.core.ReifiedInvocation
import io.github.voytech.tabulate.core.TwoParamsBasedDispatch
import io.github.voytech.tabulate.core.TwoParamsTypeInfo
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.Overflow

fun interface Operation<CTX : RenderingContext, E : AttributedContext> : InvokeWithTwoParams<CTX, E> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override operator fun invoke(renderingContext: CTX, context: E)
}

typealias ReifiedOperation<CTX, E> = ReifiedInvocation<Operation<CTX, E>, TwoParamsTypeInfo<CTX, E>>

sealed interface OperationResult
open class OverflowResult(val overflow: Overflow) : OperationResult
object Success: OperationResult
object NOOP: OperationResult

fun OperationResult?.isXOverflow(): Boolean = this is OverflowResult && overflow == Overflow.X

fun OperationResult?.isYOverflow(): Boolean = this is OverflowResult && overflow == Overflow.Y

fun <E : AttributedContext> E.setResult(result: OperationResult) = setContextAttribute("result",result)

fun <E : AttributedContext> E.getResult(): OperationResult? = getContextAttribute("result")

class Operations<CTX : RenderingContext>(private val dispatch: TwoParamsBasedDispatch) {

    private fun <E : AttributedContext> E.getAndDropResult(): OperationResult? = removeContextAttribute("result")

    private fun <CTX : RenderingContext, E : AttributedContext> Operation<CTX, E>.invokeWithResult(
        renderingContext: CTX, context: E
    ): OperationResult = invoke(renderingContext, context).let { context.getAndDropResult() ?: Success }

    operator fun <A : Attribute<*>, E : AttributedContext> invoke(renderingContext: CTX, context: E): OperationResult? =
        (dispatch[renderingContext, context] as? Operation<CTX, E>)?.invokeWithResult(renderingContext, context)

    fun isEmpty(): Boolean = dispatch.isEmpty()

}

interface Enhance<CTX : RenderingContext> {
    operator fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E>
}

class OperationsBuilder<CTX : RenderingContext>(val renderingContext: Class<CTX>) {

    private val dispatch: TwoParamsBasedDispatch = TwoParamsBasedDispatch()

    fun <E : AttributedContext> addOperation(clazz: Class<E>, op: Operation<CTX, E>) {
        with(dispatch) {
            op.bind(renderingContext, clazz)
        }
    }

    inline fun <reified E : AttributedContext> operation(op: Operation<CTX, E>) = addOperation(E::class.java, op)

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