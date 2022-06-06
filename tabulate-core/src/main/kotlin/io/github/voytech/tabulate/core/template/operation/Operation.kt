package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext
import java.util.logging.Logger

data class OperationTypeInfo<
        CTX : RenderingContext,
        ARM : Model<ARM>,
        ATTR_CAT : Attribute<*>,
        E : AttributedContext<ATTR_CAT>,
        >(
    val renderingContextType: Class<CTX>,
    val operationContextType: Class<E>,
    val attributeClassifier: AttributeClassifier<ATTR_CAT, ARM>,
)

fun interface Operation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>> {
    fun render(renderingContext: CTX, context: E)
}

open class ReifiedOperation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>, M : Model<M>>(
    internal val delegate: Operation<CTX, ATTR_CAT, E>,
    internal val typeInfo: OperationTypeInfo<CTX, M, ATTR_CAT, E>,
) : Operation<CTX, ATTR_CAT, E> by delegate

class Operations<CTX : RenderingContext>(
    private val operations: Map<Class<out AttributedContext<*>>, Operation<CTX, *, *>>,
) {

    @Suppress("UNCHECKED_CAST")
    private fun <E : AttributedContext<*>> getByContextOrNull(clazz: Class<E>): Operation<CTX, *, E>? {
        return operations[clazz] as? Operation<CTX, *, E>?
    }

    fun <A : Attribute<*>, E : AttributedContext<A>> render(renderingContext: CTX, context: E) {
        getByContextOrNull(context.javaClass)?.render(renderingContext, context)
            ?: run { logger.warning("No render operation for context class: ${context.javaClass.name} !") }
    }

    companion object {
        val logger: Logger = Logger.getLogger(Operations::class.java.name)
    }
}

fun interface Enhance<CTX : RenderingContext, M : Model<M>> : (ReifiedOperation<CTX, *, *, M>) -> (Operation<CTX, *, *>)

class OperationsBuilder<CTX : RenderingContext, M : Model<M>>(
    private val renderingContext: Class<CTX>,
    private val rootModelClass: Class<M>,
) {
    private val operations: MutableList<ReifiedOperation<CTX, *, *, M>> = mutableListOf()
    private val enhancers: MutableList<Enhance<CTX, M>> = mutableListOf()

    fun <ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>> operation(
        context: Class<E>, cat: Class<ATTR_CAT>, op: Operation<CTX, ATTR_CAT, E>,
    ) {
        operations.add(
            ReifiedOperation(op, OperationTypeInfo(renderingContext, context, AttributeClassifier(cat, rootModelClass)))
        )
    }

    inline fun <reified ATTR_CAT : Attribute<*>, reified E : AttributedContext<ATTR_CAT>> operation(op: Operation<CTX, ATTR_CAT, E>) {
        operation(E::class.java, ATTR_CAT::class.java, op)
    }

    fun enhanceOperations(enhance: Enhance<CTX, M>): OperationsBuilder<CTX, M> = apply {
        enhancers.add(enhance)
    }

    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    private inline fun <A : Attribute<*>, E : AttributedContext<A>> Enhance<CTX, M>.boundedWrap(op: ReifiedOperation<CTX, A, E, M>): ReifiedOperation<CTX, *, *, M> =
        ReifiedOperation(this(op) as Operation<CTX, A, E>, op.typeInfo)

    private fun applyEnhancers(delegate: ReifiedOperation<CTX, *, *, M>): ReifiedOperation<CTX, *, *, M> =
        enhancers.fold(delegate) { op, transformer ->
            transformer.boundedWrap(op)
        }

    @JvmSynthetic
    internal fun build(): Operations<CTX> = Operations(
        operations.map { applyEnhancers(it) }.associateBy { it.typeInfo.operationContextType }
    )
}

