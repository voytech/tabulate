package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.model.isNullOrEmpty
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.*
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

internal open class ReifiedOperation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>, M : Model<M>>(
    internal val delegate: Operation<CTX, ATTR_CAT, E>,
    internal val typeInfo: OperationTypeInfo<CTX, M, ATTR_CAT, E>,
) : Operation<CTX, ATTR_CAT, E> by delegate

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

fun <CTX : RenderingContext, ARM : Model<ARM>, ATTR_CAT : Attribute<*>, ATTR : ATTR_CAT, E : AttributedContext<ATTR_CAT>>
        AttributeOperation<CTX, ARM, ATTR_CAT, ATTR, E>.attributeClass() = typeInfo().attributeType

internal class AttributesHandlingExportOperation<CTX : RenderingContext, M : Model<M>, ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>>(
    delegate: ReifiedOperation<CTX, ATTR_CAT, E, M>,
    attributesOperations: AttributesOperations<CTX, M>,
) : ReifiedOperation<CTX, ATTR_CAT, E, M>(delegate, delegate.typeInfo) {

    private val attributeOperations: List<AttributeOperation<CTX, M, ATTR_CAT, *, E>> =
        attributesOperations.getOperationsBy(delegate.typeInfo)

    private val filteredOperationCache: AttributeClassBasedCache<ATTR_CAT, List<AttributeOperation<CTX, M, ATTR_CAT, *, E>>> =
        AttributeClassBasedCache()

    @Suppress("UNCHECKED_CAST")
    private fun <OP : AttributeOperation<CTX, M, ATTR_CAT, *, E>> E.forEachOperation(
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

    @Suppress("UNCHECKED_CAST")
    private fun <ATTR : ATTR_CAT> AttributeOperation<CTX, M, ATTR_CAT, ATTR, E>.renderAttribute(
        renderingContext: CTX,
        context: E,
        clazz: Class<out ATTR_CAT>,
    ): Boolean {
        return context.getModelAttribute(clazz)?.let {
            renderAttribute(renderingContext, context, it as ATTR).let { true }
        } ?: false
    }

    override fun render(renderingContext: CTX, context: E) {
        context.withAttributeSetBasedCache {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.forEachOperation(attributeOperations) { attributeOperation ->
                    if (attributeOperation.priority() >= 0 && !operationRendered) {
                        delegate.render(renderingContext, context)
                        operationRendered = true
                    }
                    with(attributeOperation) {
                        renderAttribute(renderingContext, context, attributeClass())
                    }
                }
            }
            if (!operationRendered) {
                delegate.render(renderingContext, context)
            }
        }
    }
}

interface Operations<CTX : RenderingContext> {
    fun <A : Attribute<*>, E : AttributedContext<A>> render(renderingContext: CTX, context: E)
}

class RenderOperations<CTX : RenderingContext>(
    private val operations: Map<Class<out AttributedContext<*>>, Operation<CTX, *, *>>,
) : Operations<CTX> {

    @Suppress("UNCHECKED_CAST")
    private fun <E : AttributedContext<*>> getByContextOrNull(clazz: Class<E>): Operation<CTX, *, E>? {
        return operations[clazz] as? Operation<CTX, *, E>?
    }

    override fun <A : Attribute<*>, E : AttributedContext<A>> render(renderingContext: CTX, context: E) {
        getByContextOrNull(context.javaClass)?.render(renderingContext, context)
            ?: run { logger.warning("No render operation for context class: ${context.javaClass.name} !") }
    }

    companion object {
        val logger: Logger = Logger.getLogger(Operations::class.java.name)
    }
}

class LayoutOperations<CTX : RenderingContext>(
    private val delegate: Operations<CTX>, private val layouts: Layouts? = null,
) : Operations<CTX> by delegate{

    private fun <A : Attribute<*>, E : AttributedContext<A>> Layout.resolveAttributeElementBoundaries(context: E): LayoutElementBoundaries? {
        context.attributes?.attributeSet?.forEach {
            if (it is LayoutElement) with(it) {
                computeBoundaries().mergeInto(context)
            }
        }
        return context.boundaries()
    }

    private fun <A : Attribute<*>, E : AttributedContext<A>> Layout.resolveElementBoundaries(context: E): LayoutElementBoundaries? {
        if (context is LayoutElement) with(context) {
            computeBoundaries().mergeInto(context)
            resolveAttributeElementBoundaries(context)
        }
        return context.boundaries()
    }

    private fun <A : Attribute<*>, E : AttributedContext<A>> Layout.commitBoundaries(
        context: E, boundaries: LayoutElementBoundaries? = null,
    ) {
        if (boundaries != null) {
            if (context is LayoutElementApply) with(context) { applyBoundaries(boundaries) }
            context.dropBoundaries()
        }
    }

    override fun <A : Attribute<*>, E : AttributedContext<A>> render(renderingContext: CTX, context: E) {
        layouts?.usingLayout {
            resolveElementBoundaries(context).let { boundaries ->
                delegate.render(renderingContext, context).also { boundaries?.applyOnLayout() }
                commitBoundaries(context, boundaries)
            }
        }
    }
}

class OperationsBuilder<CTX : RenderingContext, M : Model<M>>(
    private val renderingContext: Class<CTX>,
    private val rootModelClass: Class<M>,
    private val attributesOperations: AttributesOperations<CTX, M>
) {
    private val operations: MutableList<ReifiedOperation<CTX, *, *, M>> = mutableListOf()

    fun <ATTR_CAT : Attribute<*>, E : AttributedContext<ATTR_CAT>> operation(
        context: Class<E>, cat: Class<ATTR_CAT>, op: Operation<CTX, ATTR_CAT, E>,
    ) = ReifiedOperation(
        op, OperationTypeInfo(renderingContext, context, AttributeClassifier(cat, rootModelClass))
    ).let { operations.add(AttributesHandlingExportOperation(it, attributesOperations)) }

    inline fun <reified ATTR_CAT : Attribute<*>, reified E : AttributedContext<ATTR_CAT>> operation(op: Operation<CTX, ATTR_CAT, E>) {
        operation(E::class.java, ATTR_CAT::class.java, op)
    }

    internal fun build(): Operations<CTX> = RenderOperations(
        operations.associateBy { it.typeInfo.operationContextType }
    )
}

