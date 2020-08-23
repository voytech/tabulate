package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension

interface TableExtensionsOperations<T, A> {

    fun applyTableExtensions(state: DelegateAPI<A>, table: Table<T>, extensions: Set<TableExtension>)

    fun applyColumnExtensions(state: DelegateAPI<A>, context: OperationContext<T, ColumnOperationTableData<T>>, extensions: Set<ColumnExtension>)

    fun applyRowExtensions(state: DelegateAPI<A>, context: OperationContext<T, RowOperationTableData<T>>, extensions: Set<RowExtension>)

    fun applyCellExtensions(state: DelegateAPI<A>, context: OperationContext<T, CellOperationTableData<T>>, extensions: Set<CellExtension>)

}
