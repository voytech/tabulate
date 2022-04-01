package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.template.context.RenderingContext

/**
 * Specialised container for all discovered attribute operations.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
internal class AttributesOperationsContainer<CTX : RenderingContext> {

    private val tableAttributeRenderOperations: MutableList<AttributeOperation<CTX, *, *, *>> =
        mutableListOf()

    private val columnAttributeRenderOperations: MutableList<AttributeOperation<CTX, *, *, *>> =
        mutableListOf()

    private val rowAttributeRenderOperations: MutableList<AttributeOperation<CTX, *, *, *>> =
        mutableListOf()

    private val cellAttributeRenderOperations: MutableList<AttributeOperation<CTX, *, *, *>> =
        mutableListOf()

    private fun register(operation: AttributeOperation<CTX, *, *, *>) {
        operation.attributeClass().let { clazz ->
            when {
                TableAttribute::class.java.isAssignableFrom(clazz) ->
                    tableAttributeRenderOperations.add(operation)
                ColumnAttribute::class.java.isAssignableFrom(clazz) ->
                    columnAttributeRenderOperations.add(operation)
                RowAttribute::class.java.isAssignableFrom(clazz) ->
                    rowAttributeRenderOperations.add(operation)
                CellAttribute::class.java.isAssignableFrom(clazz) ->
                    cellAttributeRenderOperations.add(operation)
                else -> error("Unrecognised attribute level")
            }
        }
    }

    private fun Set<AttributeOperation<CTX, *, *, *>>.sortedByPriorities(): List<AttributeOperation<CTX, *, *, *>> =
        sortedBy { it.priority() }

    // TODO there is a bug because sorting is performed only on currently registered AttributeRenderOperationsFactory not entire operations collection.
    internal fun registerAttributesOperations(factory: AttributeOperationsFactory<CTX>): AttributesOperationsContainer<CTX> {
        factory.createCellAttributeRenderOperations()?.sortedByPriorities()?.forEach { register(it) }
        factory.createTableAttributeRenderOperations()?.sortedByPriorities()?.forEach { register(it) }
        factory.createRowAttributeRenderOperations()?.sortedByPriorities()?.forEach { register(it) }
        factory.createColumnAttributeRenderOperations()?.sortedByPriorities()?.forEach { register(it) }
        return this
    }

    fun <A : Attribute<*>> getOperationsBy(typeInfo: OperationTypeInfo<CTX,A,*>): List<AttributeOperation<CTX, A, *, AttributedModel<A>>>  {
        return when (typeInfo.attributeLevelType){
            TableAttribute::class.java -> tableAttributeRenderOperations
            ColumnAttribute::class.java -> columnAttributeRenderOperations
            RowAttribute::class.java -> rowAttributeRenderOperations
            CellAttribute::class.java -> cellAttributeRenderOperations
            else -> error("Requested attribute class (category) is not supported!")
        }.filter {
            it.typeInfo().operationContextClass == typeInfo.operationContextType
        }.sortedBy { it.priority() }.map { it as AttributeOperation<CTX, A, *, AttributedModel<A>> }
    }

    fun isEmpty(): Boolean =
        cellAttributeRenderOperations.isEmpty()
            .and(rowAttributeRenderOperations.isEmpty())
            .and(columnAttributeRenderOperations.isEmpty())
            .and(tableAttributeRenderOperations.isEmpty())

}