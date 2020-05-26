package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.model.hints.TableHint

interface TableHintsOperation {
    fun applyTableHints(state: DelegateAPI, hints: Set<TableHint>)
}

interface ColumnHintsOperation {
    fun applyColumnHints(state: DelegateAPI, coordinates: Coordinates, hints: Set<ColumnHint>)
}

interface RowHintsOperation {
    fun applyRowHints(state: DelegateAPI, coordinates: Coordinates, hints: Set<RowHint>)
}

interface HeaderCellsHintsOperation {
    fun applyHeaderCellHints(state: DelegateAPI, coordinates: Coordinates, hints: Set<CellHint>)
}

interface CellHintsOperation {
    fun applyCellHints(state: DelegateAPI, coordinates: Coordinates, hints: Set<CellHint>)
}
