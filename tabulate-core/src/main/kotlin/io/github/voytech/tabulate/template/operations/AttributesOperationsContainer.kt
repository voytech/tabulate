package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.template.context.RenderingContext
import java.util.logging.Logger

@Suppress("UNCHECKED_CAST")
internal class AttributesOperationsContainer<CTX: RenderingContext> {

    private val tableAttributeRenderOperationsByClass: MutableMap<Class<out TableAttribute>, TableAttributeRenderOperation<CTX, out TableAttribute>> = mutableMapOf()

    private val columnAttributeRenderOperationsByClass: MutableMap<Class<out ColumnAttribute>, ColumnAttributeRenderOperation<CTX, out ColumnAttribute>> = mutableMapOf()

    private val rowAttributeRenderOperationsByClass: MutableMap<Class<out RowAttribute>, RowAttributeRenderOperation<CTX, out RowAttribute>> = mutableMapOf()

    private val cellAttributeRenderOperationsByClass: MutableMap<Class<out CellAttribute>, CellAttributeRenderOperation<CTX, out CellAttribute>> = mutableMapOf()

    fun getCellAttributeOperation(clazz: Class<out CellAttribute>): CellAttributeRenderOperation<CTX, CellAttribute>? =
        (cellAttributeRenderOperationsByClass[clazz] as CellAttributeRenderOperation<CTX, CellAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getRowAttributeOperation(clazz: Class<out RowAttribute>): RowAttributeRenderOperation<CTX, RowAttribute>? =
        (rowAttributeRenderOperationsByClass[clazz] as RowAttributeRenderOperation<CTX, RowAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getColumnAttributeOperation(clazz: Class<out ColumnAttribute>): ColumnAttributeRenderOperation<CTX, ColumnAttribute>? =
        (columnAttributeRenderOperationsByClass[clazz] as ColumnAttributeRenderOperation<CTX, ColumnAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getTableAttributeOperation(clazz: Class<out TableAttribute>): TableAttributeRenderOperation<CTX, TableAttribute>? =
        (tableAttributeRenderOperationsByClass[clazz] as TableAttributeRenderOperation<CTX, TableAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    private fun warnNoOperation(operation: AttributeOperation<*>?, clazz: Class<out Attribute<*>>) {
        if (operation == null) logger.warning("No attribute render operation for class: ${clazz.name} !")
    }

    private fun register(operation: CellAttributeRenderOperation<CTX, out CellAttribute>) {
        cellAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    private fun register(operation: RowAttributeRenderOperation<CTX, out RowAttribute>) {
        rowAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    private fun register(operation: ColumnAttributeRenderOperation<CTX, out ColumnAttribute>) {
        columnAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    private fun register(operation: TableAttributeRenderOperation<CTX, out TableAttribute>) {
        tableAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    internal fun registerAttributesOperations(factory: AttributeRenderOperationsFactory<CTX>): AttributesOperationsContainer<CTX> {
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