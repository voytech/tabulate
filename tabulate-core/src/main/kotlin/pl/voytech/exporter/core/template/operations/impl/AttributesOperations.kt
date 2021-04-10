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
class AttributesOperations<T>(
    tableAttributeRenderOperations: Set<TableAttributeRenderOperation<out TableAttribute>>?,
    columnAttributeRenderOperations: Set<ColumnAttributeRenderOperation<T, out ColumnAttribute>>?,
    rowAttributeRenderOperations: Set<RowAttributeRenderOperation<T, out RowAttribute>>?,
    cellAttributeRenderOperations: Set<CellAttributeRenderOperation<T, out CellAttribute>>?
) {

    private val tableAttributeRenderOperationsByClass: Map<Class<out TableAttribute>, TableAttributeRenderOperation<TableAttribute>> =
        tableAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as TableAttributeRenderOperation<TableAttribute> }
            ?.toMap() ?: emptyMap()

    private val columnAttributeRenderOperationsByClass: Map<Class<out ColumnAttribute>, ColumnAttributeRenderOperation<T, ColumnAttribute>> =
        columnAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as ColumnAttributeRenderOperation<T, ColumnAttribute> }
            ?.toMap() ?: emptyMap()

    private val rowAttributeRenderOperationsByClass: Map<Class<out RowAttribute>, RowAttributeRenderOperation<T, RowAttribute>> =
        rowAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as RowAttributeRenderOperation<T, RowAttribute> }
            ?.toMap() ?: emptyMap()

    private val cellAttributeRenderOperationsByClass: Map<Class<out CellAttribute>, CellAttributeRenderOperation<T, CellAttribute>> =
        cellAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as CellAttributeRenderOperation<T, CellAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    fun getCellAttributeOperation(clazz: Class<out CellAttribute>): CellAttributeRenderOperation<T, CellAttribute>? = cellAttributeRenderOperationsByClass[clazz]

    fun getRowAttributeOperation(clazz: Class<out RowAttribute>): RowAttributeRenderOperation<T, RowAttribute>? = rowAttributeRenderOperationsByClass[clazz]

    fun getColumnAttributeOperation(clazz: Class<out ColumnAttribute>): ColumnAttributeRenderOperation<T, ColumnAttribute>? = columnAttributeRenderOperationsByClass[clazz]

    fun getTableAttributeOperation(clazz: Class<out TableAttribute>): TableAttributeRenderOperation<TableAttribute>? = tableAttributeRenderOperationsByClass[clazz]
}