package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext


typealias AttributeCategoryClass = Class<out Attribute<*>>
typealias AttributeClass = Class<out Attribute<*>>


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
 * A base class for all exporting (rendering) attribute operations associated with specific rendering contexts.
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

/**
 * Specialised container for all discovered attribute operations.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
class AttributesOperations<CTX : RenderingContext, ARM : Model<ARM>> {

    private val attributeRenderOperations: MutableMap<AttributeCategoryClass, MutableMap<AttributeClass, AttributeOperation<CTX, ARM, *, *, *>>> =
        mutableMapOf()

    internal fun register(operation: AttributeOperation<CTX, ARM, *, *, *>) {
        attributeRenderOperations.computeIfAbsent(operation.typeInfo().attributeClassifier.attributeCategory) {
            mutableMapOf()
        }[operation.typeInfo().attributeType] = operation
    }

    internal fun <A : Attribute<*>, E : AttributedContext<A>> getOperationsBy(typeInfo: OperationTypeInfo<CTX, ARM, A, E>): List<AttributeOperation<CTX, ARM, A, *, E>> {
        return attributeRenderOperations[typeInfo.attributeClassifier.attributeCategory]?.values?.filter {
            it.typeInfo().operationContextType == typeInfo.operationContextType
        }?.sortedBy { it.priority() }?.map { it as AttributeOperation<CTX, ARM, A, *, E> } ?: emptyList()
    }

    internal fun isEmpty(): Boolean = attributeRenderOperations.isEmpty()

    operator fun plusAssign(other: AttributesOperations<CTX, ARM>) {
        other.attributeRenderOperations.values.map { it.values }.flatten().forEach { register(it) }
    }

    companion object {
        fun <CTX : RenderingContext, ARM : Model<ARM>> of(vararg operations: AttributeOperation<CTX, ARM, *, *, *>): AttributesOperations<CTX, ARM> =
            AttributesOperations<CTX, ARM>().apply {
                operations.forEach { register(it) }
            }
    }

}