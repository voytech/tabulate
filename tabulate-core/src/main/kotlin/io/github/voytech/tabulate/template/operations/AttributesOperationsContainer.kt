package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import java.util.logging.Logger

@Suppress("UNCHECKED_CAST")
internal class AttributesOperationsContainer<T> {

    private val tableAttributeRenderOperationsByClass: MutableMap<Class<out TableAttribute>, TableAttributeRenderOperation<out TableAttribute>> = mutableMapOf()

    private val columnAttributeRenderOperationsByClass: MutableMap<Class<out ColumnAttribute>, ColumnAttributeRenderOperation<out ColumnAttribute>> = mutableMapOf()

    private val rowAttributeRenderOperationsByClass: MutableMap<Class<out RowAttribute>, RowAttributeRenderOperation<T,out RowAttribute>> = mutableMapOf()

    private val cellAttributeRenderOperationsByClass: MutableMap<Class<out CellAttribute>, CellAttributeRenderOperation<out CellAttribute>> = mutableMapOf()

    fun getCellAttributeOperation(clazz: Class<out CellAttribute>): CellAttributeRenderOperation<CellAttribute>? =
        (cellAttributeRenderOperationsByClass[clazz] as CellAttributeRenderOperation<CellAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getRowAttributeOperation(clazz: Class<out RowAttribute>): RowAttributeRenderOperation<T, RowAttribute>? =
        (rowAttributeRenderOperationsByClass[clazz] as RowAttributeRenderOperation<T, RowAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getColumnAttributeOperation(clazz: Class<out ColumnAttribute>): ColumnAttributeRenderOperation<ColumnAttribute>? =
        (columnAttributeRenderOperationsByClass[clazz] as ColumnAttributeRenderOperation<ColumnAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getTableAttributeOperation(clazz: Class<out TableAttribute>): TableAttributeRenderOperation<TableAttribute>? =
        (tableAttributeRenderOperationsByClass[clazz] as TableAttributeRenderOperation<TableAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    private fun warnNoOperation(operation: AttributeOperation<*>?, clazz: Class<out Attribute<*>>) {
        if (operation == null) logger.warning("No attribute render operation for class: ${clazz.name} !")
    }

    private fun register(operation: CellAttributeRenderOperation<out CellAttribute>) {
        cellAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    private fun register(operation: RowAttributeRenderOperation<T,out RowAttribute>) {
        rowAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    private fun register(operation: ColumnAttributeRenderOperation<out ColumnAttribute>) {
        columnAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    private fun register(operation: TableAttributeRenderOperation<out TableAttribute>) {
        tableAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    internal fun registerAttributesOperations(factory: AttributeRenderOperationsFactory<T>): AttributesOperationsContainer<T> {
        factory.createCellAttributeRenderOperations()?.forEach { register(it) }
        factory.createTableAttributeRenderOperations()?.forEach { register(it) }
        factory.createRowAttributeRenderOperations()?.forEach { register(it) }
        factory.createColumnAttributeRenderOperations()?.forEach { register(it) }
        return this
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