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

    private val tableAttributeRenderOperations: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    private val columnAttributeRenderOperations: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    private val rowAttributeRenderOperations: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    private val cellAttributeRenderOperations: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    internal fun register(operation: AttributeOperation<CTX, *, *, *>) {
        operation.attributeClass().let { clazz ->
            when {
                TableAttribute::class.java.isAssignableFrom(clazz) ->
                    tableAttributeRenderOperations.put(clazz, operation)
                ColumnAttribute::class.java.isAssignableFrom(clazz) ->
                    columnAttributeRenderOperations.put(clazz, operation)
                RowAttribute::class.java.isAssignableFrom(clazz) ->
                    rowAttributeRenderOperations.put(clazz, operation)
                CellAttribute::class.java.isAssignableFrom(clazz) ->
                    cellAttributeRenderOperations.put(clazz, operation)
                else -> error("Unrecognised attribute level")
            }
        }
    }

    internal fun registerAttributesOperations(factory: AttributeOperationsFactory<CTX>): AttributesOperationsContainer<CTX> = apply {
        factory.createCellAttributeRenderOperations()?.forEach { register(it) }
        factory.createTableAttributeRenderOperations()?.forEach { register(it) }
        factory.createRowAttributeRenderOperations()?.forEach { register(it) }
        factory.createColumnAttributeRenderOperations()?.forEach { register(it) }
    }

    internal fun <A : Attribute<*>, E: AttributedModel<A>> getOperationsBy(typeInfo: OperationTypeInfo<CTX,A,*>): List<AttributeOperation<CTX, A, *, E>>  {
        return when (typeInfo.attributeLevelType){
            TableAttribute::class.java -> tableAttributeRenderOperations.values
            ColumnAttribute::class.java -> columnAttributeRenderOperations.values
            RowAttribute::class.java -> rowAttributeRenderOperations.values
            CellAttribute::class.java -> cellAttributeRenderOperations.values
            else -> error("Requested attribute class (category) is not supported!")
        }.filter {
            it.typeInfo().operationContextClass == typeInfo.operationContextType
        }.sortedBy { it.priority() }.map { it as AttributeOperation<CTX, A, *, E> }
    }

    internal fun isEmpty(): Boolean =
        cellAttributeRenderOperations.isEmpty()
            .and(rowAttributeRenderOperations.isEmpty())
            .and(columnAttributeRenderOperations.isEmpty())
            .and(tableAttributeRenderOperations.isEmpty())

}