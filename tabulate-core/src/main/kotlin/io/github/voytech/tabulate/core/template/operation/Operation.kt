package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.template.RenderingContext

fun interface Operation<CTX : RenderingContext, E : AttributedContext> :
    InvokeWithTwoParams<CTX, E> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override operator fun invoke(renderingContext: CTX, context: E)
}

typealias ReifiedOperation<CTX, E> = ReifiedInvocation<Operation<CTX, E>, TwoParamsTypeInfo<CTX, E>>

@JvmInline
value class Operations<CTX : RenderingContext>(private val dispatch: TwoParamsBasedDispatch) {

    fun <A : Attribute<*>, E : AttributedContext> render(renderingContext: CTX, context: E) {
        dispatch(renderingContext, context)
    }

}

interface Enhance<CTX : RenderingContext> {
    operator fun <E : AttributedContext> invoke(op: ReifiedOperation<CTX, E>): Operation<CTX, E>
}

class OperationsBuilder<CTX : RenderingContext>(val renderingContext: Class<CTX>) {
    private val dispatch: TwoParamsBasedDispatch = TwoParamsBasedDispatch()

    fun <E : AttributedContext> addOperation(clazz: Class<E>,op: Operation<CTX, E>) {
        with(dispatch) {
            op.bind(renderingContext, clazz)
        }
    }

    inline fun <reified E : AttributedContext> operation(op: Operation<CTX, E>) = addOperation(E::class.java, op)

    @Suppress("UNCHECKED_CAST")
    private fun List<Enhance<CTX>>.applyEnhancers(delegate: ReifiedInvocation<*, *>) =
        fold(delegate as ReifiedOperation<CTX, AttributedContext>) { op, transformer ->
            ReifiedOperation(op.meta, transformer.invoke(op))
        }.delegate

    @JvmSynthetic
    internal fun build(vararg enhancers: Enhance<CTX>): Operations<CTX> {
        dispatch.transform { enhancers.toList().applyEnhancers(it) }
        return Operations(dispatch)
    }
}