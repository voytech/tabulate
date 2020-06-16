package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension

interface TableExtensionsOperation<A,E> {
    fun applyTableExtensions(state: DelegateAPI<A>, table: Table<E>, extensions: Set<TableExtension>)
}

interface ColumnExtensionsOperation<A> {
    fun applyColumnExtensions(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<ColumnExtension>)
}

interface RowExtensionsOperation<A> {
    fun applyRowExtensions(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<RowExtension>)
}

interface CellExtensionsOperation<A> {
    fun applyCellExtensions(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<CellExtension>)
}
