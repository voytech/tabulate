package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class DelegatingAttributesOperations<T, A>(
    private val tableAttributeOperations: List<TableAttributeOperation<out TableAttribute, A>>?,
    private val columnAttributeOperations: List<ColumnAttributeOperation<T, out ColumnAttribute, A>>?,
    private val rowAttributeOperations: List<RowAttributeOperation<T, out RowAttribute, A>>?,
    private val cellAttributeOperations: List<CellAttributeOperation<T, out CellAttribute, A>>?
) : TableAttributesOperations<T, A> {

    private fun tableAttributeOperationByClass(hint: KClass<out TableAttribute>): TableAttributeOperation<TableAttribute, A>? {
        return tableAttributeOperations?.find { operation -> operation.attributeType() == hint } as TableAttributeOperation<TableAttribute, A>
    }

    private fun rowAttributeOperationByClass(hint: KClass<out RowAttribute>): RowAttributeOperation<T, RowAttribute, A>? {
        return rowAttributeOperations?.find { operation -> operation.attributeType() == hint } as RowAttributeOperation<T, RowAttribute, A>
    }

    private fun columnAttributeOperationByClass(hint: KClass<out ColumnAttribute>): ColumnAttributeOperation<T, ColumnAttribute, A>? {
        return columnAttributeOperations?.find { operation -> operation.attributeType() == hint } as ColumnAttributeOperation<T, ColumnAttribute, A>
    }

    private fun cellAttributeOperationByClass(hint: KClass<out CellAttribute>): CellAttributeOperation<T, CellAttribute, A>? {
        return cellAttributeOperations?.find { operation -> operation.attributeType() == hint } as CellAttributeOperation<T, CellAttribute, A>
    }


    override fun applyTableAttributes(state: DelegateAPI<A>, table: Table<T>, attributes: Set<TableAttribute>) {
        attributes.forEach { hint ->
            tableAttributeOperationByClass(hint::class)?.renderAttribute(state, table, hint)
        }
    }

    override fun applyColumnAttributes(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        attributes: Set<ColumnAttribute>
    ) {
        attributes.forEach { hint ->
            columnAttributeOperationByClass(hint::class)?.renderAttribute(state, context, hint)
        }
    }

    override fun applyRowAttributes(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        attributes: Set<RowAttribute>
    ) {
        attributes.forEach { hint ->
            rowAttributeOperationByClass(hint::class)?.renderAttribute(state, context, hint)
        }
    }

    override fun applyCellAttributes(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        attributes: Set<CellAttribute>
    ) {
        attributes.forEach { hint ->
            cellAttributeOperationByClass(hint::class)?.renderAttribute(state, context, hint)
        }
    }
}