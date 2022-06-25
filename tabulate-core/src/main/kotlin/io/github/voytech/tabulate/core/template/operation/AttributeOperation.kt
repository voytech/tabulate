package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.InvokeWithThreeParams
import io.github.voytech.tabulate.core.ReifiedInvocation
import io.github.voytech.tabulate.core.ThreeParamsBasedDispatch
import io.github.voytech.tabulate.core.TypesInfo
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.template.RenderingContext

/**
 * A base class for all exporting (rendering) attribute operations associated with specific rendering contexts.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface AttributeOperation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, ATTR : ATTR_CAT, E : AttributedContext<ATTR_CAT>> :
    InvokeWithThreeParams<CTX, E, ATTR> {
    fun priority(): Int = DEFAULT

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override operator fun invoke(renderingContext: CTX, context: E, attribute: ATTR)

    companion object {
        const val LOWEST = Int.MIN_VALUE
        const val LOWER = -1
        const val DEFAULT = 1
    }
}

typealias ReifiedAttributeOperation<CTX, CAT, ATTR, E> = ReifiedInvocation<AttributeOperation<CTX, CAT, ATTR, E>>

fun <CTX : RenderingContext, ATTR_CAT : Attribute<*>, ATTR: ATTR_CAT, E : AttributedContext<ATTR_CAT>>
        ReifiedAttributeOperation<CTX, ATTR_CAT, *, E>.attributeClass(): Class<ATTR> = meta.types.last() as Class<ATTR>

/**
 * Specialised container for all discovered attribute operations.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmInline
value class AttributesOperations<CTX : RenderingContext>(private val dispatch: ThreeParamsBasedDispatch) {

    @Suppress("UNCHECKED_CAST")
    internal fun <A : Attribute<*>, E : AttributedContext<A>> getOperationsBy(typeInfo: TypesInfo): List<ReifiedAttributeOperation<CTX, A, *, E>> {
        return dispatch.find { it.dropLast(1) == typeInfo }.map { it as ReifiedAttributeOperation<CTX, A, *, E> }
            .sortedBy { it.delegate.priority() }
    }

    internal fun isEmpty(): Boolean = dispatch.isEmpty()

}

class AttributeOperationsBuilder<CTX : RenderingContext>(val renderingContext: Class<CTX>) {
    val dispatch: ThreeParamsBasedDispatch = ThreeParamsBasedDispatch()
    inline fun <ATTR_CAT : Attribute<*>, reified E : AttributedContext<ATTR_CAT>, reified ATTR: ATTR_CAT> operation(op: AttributeOperation<CTX, ATTR_CAT, ATTR, E>) {
        with(dispatch) {
            op.bind(renderingContext, E::class.java, ATTR::class.java)
        }
    }

    @JvmSynthetic
    internal fun build(): AttributesOperations<CTX> = AttributesOperations(dispatch)

}