package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.model.isNullOrEmpty
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.LayoutElement
import io.github.voytech.tabulate.core.template.layout.Layouts
import io.github.voytech.tabulate.core.template.layout.dropBoundaries
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

data class AttributeOperationTypeInfo<
        CTX : RenderingContext,
        ARM : Model<ARM>,
        ATTR_CAT : Attribute<*>,
        ATTR : ATTR_CAT,
        E : AttributedContext<ATTR_CAT>,
        >(
    val renderingContextType: Class<CTX>,
    val operationContextType: Class<E>,
    val attributeClassifier: AttributeClassifier<ATTR_CAT, ARM>,
    val attributeType: Class<ATTR>,
)

/**
 * A base class for all exporting (rendering) attribute operations associated for specific rendering contexts.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface AttributeOperation<CTX : RenderingContext, ARM : Model<ARM>, ATTR_CAT : Attribute<*>, ATTR : ATTR_CAT, E : AttributedContext<ATTR_CAT>> {
    fun typeInfo(): AttributeOperationTypeInfo<CTX, ARM, ATTR_CAT, ATTR, E>
    fun priority(): Int = DEFAULT
    fun renderAttribute(renderingContext: CTX, context: E, attribute: ATTR)

    companion object {
        const val LOWEST = Int.MIN_VALUE
        const val LOWER = -1
        const val DEFAULT = 1
    }
}

abstract class AbstractAttributeOperation<
        CTX : RenderingContext,
        ARM : Model<ARM>,
        ATTR_CAT : Attribute<*>,
        ATTR : ATTR_CAT,
        E : AttributedContext<ATTR_CAT>,
        > : AttributeOperation<CTX, ARM, ATTR_CAT, ATTR, E> {
    override fun typeInfo(): AttributeOperationTypeInfo<CTX, ARM, ATTR_CAT, ATTR, E> =
        AttributeOperationTypeInfo(
            renderingContextClass(),
            operationContextClass(),
            classifier(),
            attributeClass()
        )

    abstract fun renderingContextClass(): Class<CTX>

    abstract fun operationContextClass(): Class<E>

    abstract fun attributeClass(): Class<ATTR>

    abstract fun classifier(): AttributeClassifier<ATTR_CAT, ARM>

    companion object {
        const val LOWEST = Int.MIN_VALUE
        const val LOWER = -1
        const val DEFAULT = 1
    }
}

fun <
        CTX : RenderingContext,
        ARM : Model<ARM>,
        ATTR_CAT : Attribute<*>,
        ATTR : ATTR_CAT,
        E : AttributedContext<ATTR_CAT>,
        > AttributeOperation<CTX, ARM, ATTR_CAT, ATTR, E>.attributeClass() =
    typeInfo().attributeType


internal class AttributesHandlingOperation<
        CTX : RenderingContext,
        ARM : Model<ARM>,
        ATTR_CAT : Attribute<*>,
        E : AttributedContext<ATTR_CAT>,
        >(
    typeInfo: OperationTypeInfo<CTX, ARM, ATTR_CAT, E>,
    private val operation: Operation<CTX, ATTR_CAT, E>,
    attributeOperationsContainer: AttributesOperationsContainer<CTX, ARM>,
    private val enableAttributeSetBasedCaching: Boolean = true,
) : Operation<CTX, ATTR_CAT, E> {

    private val attributeOperations: List<AttributeOperation<CTX, ARM, ATTR_CAT, *, E>> =
        attributeOperationsContainer.getOperationsBy(typeInfo)

    private val filteredOperationCache: AttributeClassBasedCache<ATTR_CAT, List<AttributeOperation<CTX, ARM, ATTR_CAT, *, E>>> =
        AttributeClassBasedCache()

    @Suppress("UNCHECKED_CAST")
    private fun <OP : AttributeOperation<CTX, ARM, ATTR_CAT, *, E>> E.forEachOperation(
        unfiltered: List<OP>, consumer: (operation: OP) -> Boolean,
    ) {
        attributes?.let { _attributes ->
            if (_attributes.isNotEmpty()) {
                if (filteredOperationCache[_attributes].isNullOrEmpty()) {
                    filteredOperationCache[_attributes] = unfiltered.filterTo(mutableListOf()) { consumer(it) }
                } else {
                    filteredOperationCache[_attributes]!!.forEach { consumer(it as OP) }
                }
            }
        }
    }

    private fun E.withAttributeSetCacheIfEnabled(block: () -> Unit) {
        if (enableAttributeSetBasedCaching) withAttributeSetBasedCache { block() } else block()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <ATTR : ATTR_CAT> AttributeOperation<CTX, ARM, ATTR_CAT, ATTR, E>.renderAttribute(
        renderingContext: CTX,
        context: E,
        clazz: Class<out ATTR_CAT>,
    ): Boolean {
        return context.getModelAttribute(clazz)?.let {
            renderAttribute(renderingContext, context, it as ATTR).let { true }
        } ?: false
    }

    override fun render(renderingContext: CTX, context: E) {
        context.withAttributeSetCacheIfEnabled {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.forEachOperation(attributeOperations) { attributeOperation ->
                    if (attributeOperation.priority() >= 0 && !operationRendered) {
                        operation.render(renderingContext, context)
                        operationRendered = true
                    }
                    with(attributeOperation) {
                        renderAttribute(renderingContext, context, attributeClass())
                    }
                }
            }
            if (!operationRendered) {
                operation.render(renderingContext, context)
            }
        }
    }
}

data class Operations<CTX : RenderingContext>(
    internal val operationsByContextClass: Map<Class<out AttributedContext<*>>, Operation<CTX, *, *>>,
) {
    internal var layouts: Layouts? = null

    @Suppress("UNCHECKED_CAST")
    private fun <E : AttributedContext<*>> getByContextOrNull(clazz: Class<E>): Operation<CTX, *, E>? {
        return operationsByContextClass[clazz] as? Operation<CTX, *, E>?
    }

    private fun <A : Attribute<*>, E : AttributedContext<A>> Operation<CTX, *, E>.renderWithLayout(renderingContext: CTX, context: E) {
        layouts?.usingLayout {
            with(context as LayoutElement) {
                computeBoundaries().let { boundaries ->
                    boundaries.into(context)
                    this@renderWithLayout.render(renderingContext, context)
                    boundaries.applyOnLayout()
                    applyBoundaries(boundaries)
                    context.dropBoundaries()
                }
            }
        }
    }

    fun <A : Attribute<*>, E : AttributedContext<A>> render(renderingContext: CTX, context: E) {
        getByContextOrNull(context.javaClass)?.let { op ->
            if (context is LayoutElement) {
                op.renderWithLayout(renderingContext, context)
            } else {
                op.render(renderingContext, context)
            }
        } ?: run { logger.warning("No render operation for context class: ${context.javaClass.name} !") }
    }

    companion object {
        val logger: Logger = Logger.getLogger(Operations::class.java.name)
    }
}

internal typealias ReifiedOperation<CTX, ARM, ATTR_CAT, E> = Pair<OperationTypeInfo<CTX, ARM, ATTR_CAT, E>, Operation<CTX, ATTR_CAT, E>>

class OperationsBuilder<CTX : RenderingContext, ARM : Model<ARM>>(
    private val renderingContext: Class<CTX>,
    private val rootModelClass: Class<ARM>,
    private val attributesOperations: AttributesOperationsContainer<CTX, ARM>? = null,
) {
    private val operations: MutableList<ReifiedOperation<CTX, ARM, *, *>> = mutableListOf()

    fun <ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>> operation(
        context: Class<E>, cat: Class<ATTR_CAT>, op: Operation<CTX, ATTR_CAT, E>,
    ) {
        OperationTypeInfo(renderingContext, context, AttributeClassifier(cat, rootModelClass)).let { info ->
            attributesOperations?.let { container ->
                info to AttributesHandlingOperation(info, op, container)
            } ?: (info to op)
        }.let {
            operations.add(it)
        }
    }

    inline fun <reified ATTR_CAT : Attribute<*>, reified E : AttributedContext<ATTR_CAT>> operation(op: Operation<CTX, ATTR_CAT, E>) {
        operation(E::class.java, ATTR_CAT::class.java, op)
    }

    internal fun build(): Operations<CTX> = Operations(
        operations.associate { it.first.operationContextType to it.second }
    )
}

