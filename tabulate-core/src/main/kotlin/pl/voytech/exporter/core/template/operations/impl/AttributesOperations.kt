package pl.voytech.exporter.core.template.operations.impl

import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.model.attributes.alias.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.alias.RowAttribute
import pl.voytech.exporter.core.model.attributes.alias.TableAttribute
import pl.voytech.exporter.core.template.operations.CellAttributeRenderOperation
import pl.voytech.exporter.core.template.operations.ColumnAttributeRenderOperation
import pl.voytech.exporter.core.template.operations.RowAttributeRenderOperation
import pl.voytech.exporter.core.template.operations.TableAttributeRenderOperation

@Suppress("UNCHECKED_CAST")
class AttributesOperations<T> {

    private val tableAttributeRenderOperationsByClass: MutableMap<Class<out TableAttribute>, TableAttributeRenderOperation<out TableAttribute>> = mutableMapOf()

    private val columnAttributeRenderOperationsByClass: MutableMap<Class<out ColumnAttribute>, ColumnAttributeRenderOperation<T,out ColumnAttribute>> = mutableMapOf()

    private val rowAttributeRenderOperationsByClass: MutableMap<Class<out RowAttribute>, RowAttributeRenderOperation<T,out RowAttribute>> = mutableMapOf()

    private val cellAttributeRenderOperationsByClass: MutableMap<Class<out CellAttribute>, CellAttributeRenderOperation<T, out CellAttribute>> = mutableMapOf()

    fun getCellAttributeOperation(clazz: Class<out CellAttribute>): CellAttributeRenderOperation<T, CellAttribute>? =
        cellAttributeRenderOperationsByClass[clazz] as ( CellAttributeRenderOperation<T, CellAttribute>?)

    fun getRowAttributeOperation(clazz: Class<out RowAttribute>): RowAttributeRenderOperation<T, RowAttribute>? =
        rowAttributeRenderOperationsByClass[clazz] as RowAttributeRenderOperation<T, RowAttribute>?

    fun getColumnAttributeOperation(clazz: Class<out ColumnAttribute>): ColumnAttributeRenderOperation<T, ColumnAttribute>? =
        columnAttributeRenderOperationsByClass[clazz] as ColumnAttributeRenderOperation<T, ColumnAttribute>?

    fun getTableAttributeOperation(clazz: Class<out TableAttribute>): TableAttributeRenderOperation<TableAttribute>? =
        tableAttributeRenderOperationsByClass[clazz] as TableAttributeRenderOperation<TableAttribute>?

    fun register(operation: CellAttributeRenderOperation<T, out CellAttribute>) {
        cellAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    fun register(operation: RowAttributeRenderOperation<T,out RowAttribute>) {
        rowAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    fun register(operation: ColumnAttributeRenderOperation<T,out ColumnAttribute>) {
        columnAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    fun register(operation: TableAttributeRenderOperation<out TableAttribute>) {
        tableAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    fun isEmpty(): Boolean =
        cellAttributeRenderOperationsByClass.isEmpty()
            .and(rowAttributeRenderOperationsByClass.isEmpty())
            .and(columnAttributeRenderOperationsByClass.isEmpty())
            .and(tableAttributeRenderOperationsByClass.isEmpty())
}