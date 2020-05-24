package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.model.hints.TableHint

interface TableHintsOperation {
    fun applyTableHints(state: DelegateState, hints: Set<TableHint>)
}

interface ColumnHintsOperation {
    fun applyColumnHints(state: DelegateState, columnIndex: Int, hints: Set<ColumnHint>)
}

interface RowHintsOperation {
    fun applyRowHints(state: DelegateState, rowIndex: Int, hints: Set<RowHint>)
}

interface HeaderCellsHintsOperation {
    fun applyHeaderCellHints(state: DelegateState, columnIndex: Int, hints: Set<CellHint>)
}

interface CellHintsOperation {
    fun applyCellHints(state: DelegateState, coordinates: Coordinates, hints: Set<CellHint>)
}
