package io.github.voytech.tabulate.template.operations.impl

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.template.operations.CellAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.ColumnAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.RowAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.TableAttributeRenderOperation

@Suppress("UNCHECKED_CAST")
class AttributesOperations<T> {

    private val tableAttributeRenderOperationsByClass: MutableMap<Class<out TableAttribute>, TableAttributeRenderOperation<out TableAttribute>> = mutableMapOf()

    private val columnAttributeRenderOperationsByClass: MutableMap<Class<out ColumnAttribute>, ColumnAttributeRenderOperation<out ColumnAttribute>> = mutableMapOf()

    private val rowAttributeRenderOperationsByClass: MutableMap<Class<out RowAttribute>, RowAttributeRenderOperation<T,out RowAttribute>> = mutableMapOf()

    private val cellAttributeRenderOperationsByClass: MutableMap<Class<out CellAttribute>, CellAttributeRenderOperation<out CellAttribute>> = mutableMapOf()

    fun getCellAttributeOperation(clazz: Class<out CellAttribute>): CellAttributeRenderOperation<CellAttribute>? =
        cellAttributeRenderOperationsByClass[clazz] as ( CellAttributeRenderOperation<CellAttribute>?)

    fun getRowAttributeOperation(clazz: Class<out RowAttribute>): RowAttributeRenderOperation<T, RowAttribute>? =
        rowAttributeRenderOperationsByClass[clazz] as RowAttributeRenderOperation<T, RowAttribute>?

    fun getColumnAttributeOperation(clazz: Class<out ColumnAttribute>): ColumnAttributeRenderOperation<ColumnAttribute>? =
        columnAttributeRenderOperationsByClass[clazz] as ColumnAttributeRenderOperation<ColumnAttribute>?

    fun getTableAttributeOperation(clazz: Class<out TableAttribute>): TableAttributeRenderOperation<TableAttribute>? =
        tableAttributeRenderOperationsByClass[clazz] as TableAttributeRenderOperation<TableAttribute>?

    fun register(operation: CellAttributeRenderOperation<out CellAttribute>) {
        cellAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    fun register(operation: RowAttributeRenderOperation<T,out RowAttribute>) {
        rowAttributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    fun register(operation: ColumnAttributeRenderOperation<out ColumnAttribute>) {
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