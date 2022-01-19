package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.template.context.RenderingContext
import java.util.logging.Logger

/**
 * Specialised container for all discovered attribute operations.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
internal class AttributesOperationsContainer<CTX : RenderingContext> {

    private val tableAttributeRenderOperationsByClass: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    private val columnAttributeRenderOperationsByClass: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    private val rowAttributeRenderOperationsByClass: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    private val cellAttributeRenderOperationsByClass: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    fun getCellAttributeOperation(clazz: Class<out CellAttribute<*>>): CellAttributeRenderOperation<CTX, CellAttribute<*>>? =
        (cellAttributeRenderOperationsByClass[clazz] as CellAttributeRenderOperation<CTX, CellAttribute<*>>?).also {
            warnNoOperation(it, clazz)
        }

    fun getRowAttributeOperation(clazz: Class<out RowAttribute<*>>): RowAttributeRenderOperation<CTX, RowAttribute<*>>? =
        (rowAttributeRenderOperationsByClass[clazz] as RowAttributeRenderOperation<CTX, RowAttribute<*>>?).also {
            warnNoOperation(it, clazz)
        }

    fun getColumnAttributeOperation(clazz: Class<out ColumnAttribute<*>>): ColumnAttributeRenderOperation<CTX, ColumnAttribute<*>>? =
        (columnAttributeRenderOperationsByClass[clazz] as ColumnAttributeRenderOperation<CTX, ColumnAttribute<*>>?).also {
            warnNoOperation(it, clazz)
        }

    fun getTableAttributeOperation(clazz: Class<out TableAttribute<*>>): TableAttributeRenderOperation<CTX, TableAttribute<*>>? =
        (tableAttributeRenderOperationsByClass[clazz] as TableAttributeRenderOperation<CTX, TableAttribute<*>>?).also {
            warnNoOperation(it, clazz)
        }

    private fun warnNoOperation(operation: AttributeOperation<*, *, *, *>?, clazz: Class<out Attribute<*>>) {
        if (operation == null) logger.warning("No attribute render operation for class: ${clazz.name} !")
    }

    private fun register(operation: AttributeOperation<CTX, *, *, *>) {
        operation.attributeType().let { clazz ->
            when {
                TableAttribute::class.java.isAssignableFrom(clazz) ->
                    tableAttributeRenderOperationsByClass[operation.attributeType()] = operation
                ColumnAttribute::class.java.isAssignableFrom(clazz) ->
                    columnAttributeRenderOperationsByClass[operation.attributeType()] = operation
                RowAttribute::class.java.isAssignableFrom(clazz) ->
                    rowAttributeRenderOperationsByClass[operation.attributeType()] = operation
                CellAttribute::class.java.isAssignableFrom(clazz) ->
                    cellAttributeRenderOperationsByClass[operation.attributeType()] = operation
            }
        }
    }

    private fun Set<AttributeOperation<CTX, *, *, *>>.sortedByPriorities(): List<AttributeOperation<CTX, *, *, *>> =
        sortedBy { it.priority() }

    // TODO there is a bug because sorting is performed only on currently registered AttributeRenderOperationsFactory not entire operations collection.
    internal fun registerAttributesOperations(factory: AttributeRenderOperationsFactory<CTX>): AttributesOperationsContainer<CTX> {
        factory.createCellAttributeRenderOperations()?.sortedByPriorities()?.forEach { register(it) }
        factory.createTableAttributeRenderOperations()?.sortedByPriorities()?.forEach { register(it) }
        factory.createRowAttributeRenderOperations()?.sortedByPriorities()?.forEach { register(it) }
        factory.createColumnAttributeRenderOperations()?.sortedByPriorities()?.forEach { register(it) }
        return this
    }

    inline fun <reified A : Attribute<*>> getOperationsBy(): List<AttributeOperation<CTX, *, *, *>> {
        return when {
            TableAttribute::class.java == A::class.java -> tableAttributeRenderOperationsByClass.values.toList()
            ColumnAttribute::class.java == A::class.java -> columnAttributeRenderOperationsByClass.values.toList()
            RowAttribute::class.java == A::class.java -> rowAttributeRenderOperationsByClass.values.toList()
            CellAttribute::class.java == A::class.java -> cellAttributeRenderOperationsByClass.values.toList()
            else -> error("Requested attribute class (category) is not supported!")
        }
    }

    fun isEmpty(): Boolean =
        cellAttributeRenderOperationsByClass.isEmpty()
            .and(rowAttributeRenderOperationsByClass.isEmpty())
            .and(columnAttributeRenderOperationsByClass.isEmpty())
            .and(tableAttributeRenderOperationsByClass.isEmpty())

    companion object {
        val logger: Logger = Logger.getLogger(AttributesOperationsContainer::class.java.name)
    }
}