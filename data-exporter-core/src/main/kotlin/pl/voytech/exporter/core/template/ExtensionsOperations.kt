package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension

interface TableExtensionsOperation<T, A> {
    fun applyTableExtensions(state: DelegateAPI<A>, table: Table<T>, extensions: Set<TableExtension>)
}

interface ColumnExtensionsOperation<T, A> {
    fun applyColumnExtensions(state: DelegateAPI<A>, context: OperationContext<T, ColumnOperationTableData<T>>, extensions: Set<ColumnExtension>)
}

interface RowExtensionsOperation<T, A> {
    fun applyRowExtensions(state: DelegateAPI<A>, context: OperationContext<T, RowOperationTableData<T>>, extensions: Set<RowExtension>)
}

interface CellExtensionsOperation<T, A> {
    fun applyCellExtensions(state: DelegateAPI<A>, context: OperationContext<T, CellOperationTableData<T>>, extensions: Set<CellExtension>)
}
