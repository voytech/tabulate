package io.github.voytech.tabulate.core.template.operation

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.RenderingContext

/**
 * Specialised container for all discovered attribute operations.
 * @author Wojciech Mąka
 * @since 0.1.0
 */

typealias AttributeCategoryClass = Class<out Attribute<*>>
typealias AttributeClass = Class<out Attribute<*>>

@Suppress("UNCHECKED_CAST")
class AttributesOperations<CTX : RenderingContext, ARM : Model<ARM>> {

    private val attributeRenderOperations: MutableMap<AttributeCategoryClass, MutableMap<AttributeClass, AttributeOperation<CTX, ARM, *, *, *>>> =
        mutableMapOf()

    internal fun register(operation: AttributeOperation<CTX, ARM,*, *, *>) {
        attributeRenderOperations.computeIfAbsent(operation.typeInfo().attributeClassifier.attributeCategory) {
            mutableMapOf()
        }[operation.typeInfo().attributeType] = operation
    }

    internal fun <A : Attribute<*>, E: AttributedContext<A>> getOperationsBy(typeInfo: OperationTypeInfo<CTX,ARM,A,E>): List<AttributeOperation<CTX, ARM, A, *, E>>  {
        return attributeRenderOperations[typeInfo.attributeClassifier.attributeCategory]?.values?.filter {
            it.typeInfo().operationContextType == typeInfo.operationContextType
        }?.sortedBy { it.priority() }?.map { it as AttributeOperation<CTX,ARM, A, *, E> } ?: emptyList()
    }

    internal fun isEmpty(): Boolean = attributeRenderOperations.isEmpty()

    operator fun plusAssign(other: AttributesOperations<CTX, ARM>) {
        attributeRenderOperations += other.attributeRenderOperations //TODO implement correctly
    }

    companion object {
        fun <CTX : RenderingContext, ARM : Model<ARM>> of(vararg operations: AttributeOperation<CTX, ARM, *, *, *>): AttributesOperations<CTX, ARM> =
            AttributesOperations<CTX, ARM>().apply {
                operations.forEach { register(it) }
            }
    }

}