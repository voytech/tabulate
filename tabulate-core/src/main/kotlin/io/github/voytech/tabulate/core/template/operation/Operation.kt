package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.InvokeWithTwoParams
import io.github.voytech.tabulate.core.ReifiedInvocation
import io.github.voytech.tabulate.core.TwoParamsBasedDispatch
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.template.RenderingContext


fun interface Operation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>> :
    InvokeWithTwoParams<CTX, E> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override operator fun invoke(renderingContext: CTX, context: E)
}

typealias ReifiedOperation<CTX, CAT, E> = ReifiedInvocation<Operation<CTX, CAT, E>>

@JvmInline
value class Operations<CTX : RenderingContext>(private val dispatch: TwoParamsBasedDispatch) {

    fun <A : Attribute<*>, E : AttributedContext<A>> render(renderingContext: CTX, context: E) {
        dispatch(renderingContext, context)
    }

}

interface Enhance<CTX : RenderingContext> {
    operator fun <ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>> invoke(op: ReifiedOperation<CTX, ATTR_CAT, E>): Operation<CTX, ATTR_CAT, E>
}

class OperationsBuilder<CTX : RenderingContext>(val renderingContext: Class<CTX>) {
    val dispatch: TwoParamsBasedDispatch = TwoParamsBasedDispatch()

    inline fun <ATTR_CAT : Attribute<*>, reified E : AttributedContext<ATTR_CAT>> operation(op: Operation<CTX, ATTR_CAT, E>) {
        with(dispatch) {
            op.bind(renderingContext, E::class.java)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <CAT : Attribute<*>, E : AttributedContext<CAT>> List<Enhance<CTX>>.applyEnhancers(delegate: ReifiedInvocation<InvokeWithTwoParams<*, *>>): Operation<CTX, CAT, E> =
        fold(delegate as ReifiedOperation<CTX, CAT, E>) { op, transformer ->
            ReifiedOperation(op.meta, transformer.invoke(op))
        }.delegate

    @JvmSynthetic
    internal fun build(vararg enhancers: Enhance<CTX>): Operations<CTX> {
        dispatch.transform { enhancers.toList().applyEnhancers(it) }
        return Operations(dispatch)
    }
}