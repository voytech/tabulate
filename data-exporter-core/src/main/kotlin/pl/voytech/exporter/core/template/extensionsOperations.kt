package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute

interface TableAttributesOperations<T, A> {

    fun applyTableAttributes(state: DelegateAPI<A>, table: Table<T>, attributes: Set<TableAttribute>)

    fun applyColumnAttributes(state: DelegateAPI<A>, context: OperationContext<T, ColumnOperationTableData<T>>, attributes: Set<ColumnAttribute>)

    fun applyRowAttributes(state: DelegateAPI<A>, context: OperationContext<T, RowOperationTableData<T>>, attributes: Set<RowAttribute>)

    fun applyCellAttributes(state: DelegateAPI<A>, context: OperationContext<T, CellOperationTableData<T>>, attributes: Set<CellAttribute>)

}
