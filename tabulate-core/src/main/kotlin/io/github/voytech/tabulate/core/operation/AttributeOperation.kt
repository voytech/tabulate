package io.github.voytech.tabulate.core.operation

import io.github.voytech.tabulate.core.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.RenderingContext

/**
 * A base class for all exporting (rendering) attribute operations associated with specific rendering contexts.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun interface AttributeOperation<CTX : RenderingContext, A : Attribute<A>, E : AttributedContext> :
    InvokeWithThreeParams<CTX, E, A> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override operator fun invoke(renderingContext: CTX, context: E, attribute: A)

}

internal class PrioritizedAttributeOperation<CTX : RenderingContext, A : Attribute<A>, E : AttributedContext>(
    internal val priority: Int = DEFAULT,
    internal val operation: AttributeOperation<CTX, A, E>
) : AttributeOperation<CTX, A, E> {

    override fun invoke(renderingContext: CTX, context: E, attribute: A) =
        operation(renderingContext, context, attribute)

    companion object {
        const val LOWEST = Int.MIN_VALUE
        const val LOWER = -1
        const val DEFAULT = 1
    }
}

typealias ReifiedAttributeOperation<CTX, A, E> = ReifiedInvocation<AttributeOperation<CTX, A, E>, ThreeParamsTypeInfo<CTX, E, A>>

/**
 * Specialised container for all discovered attribute operations.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmInline
value class AttributesOperations<CTX : RenderingContext>(private val dispatch: ThreeParamsBasedDispatch) {

    @Suppress("UNCHECKED_CAST")
    internal fun <E : AttributedContext> getOperationsBy(info: TwoParamsTypeInfo<CTX, E>): List<ReifiedAttributeOperation<CTX, *, E>> {
        return dispatch.find { it.firstTwoParamTypes() == info }.map { it as ReifiedAttributeOperation<CTX, *, E> }
            .sortedBy { (it.delegate as PrioritizedAttributeOperation<CTX, *, E>).priority }
    }
}

class AttributeOperationsBuilder<CTX : RenderingContext>(val renderingContext: Class<CTX>) {
    val dispatch: ThreeParamsBasedDispatch = ThreeParamsBasedDispatch()

    fun <A : Attribute<A>, E : AttributedContext> AttributeOperation<CTX, A, E>.prioritized(
        priority: Int
    ): AttributeOperation<CTX, A, E> = PrioritizedAttributeOperation(priority, this)


    inline fun <reified A : Attribute<A>, reified E : AttributedContext> operation(
        op: AttributeOperation<CTX, A, E>, priority: Int = 1
    ) {
        with(dispatch) {
            op.prioritized(priority).bind(renderingContext, E::class.java, A::class.java)
        }
    }

    @JvmSynthetic
    internal fun build(): AttributesOperations<CTX> = AttributesOperations(dispatch)

}