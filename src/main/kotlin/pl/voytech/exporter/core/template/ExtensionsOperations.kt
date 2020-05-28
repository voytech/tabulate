package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension

interface TableExtensionsOperation {
    fun applyTableExtensions(state: DelegateAPI, extensions: Set<TableExtension>)
}

interface ColumnExtensionsOperation {
    fun applyColumnExtensions(state: DelegateAPI, coordinates: Coordinates, extensions: Set<ColumnExtension>)
}

interface RowExtensionsOperation {
    fun applyRowExtensions(state: DelegateAPI, coordinates: Coordinates, extensions: Set<RowExtension>)
}

interface CellExtensionsOperation {
    fun applyCellExtensions(state: DelegateAPI, coordinates: Coordinates, extensions: Set<CellExtension>)
}
