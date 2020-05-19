package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.model.hints.TableHint

interface HintsOperations {
    fun applyTableHints(state: DelegateState, hints: Set<TableHint>)
    fun applyColumnHints(state: DelegateState, columnIndex: Int, hints: Set<CellHint>)
    fun applyColumnHeaderCellHints(state: DelegateState, columnIndex: Int, hints: Set<CellHint>)
    fun applyRowHints(state: DelegateState, rowIndex: Int, hints: Set<RowHint>)
    fun applyCellHints(state: DelegateState, coordinates: Coordinates, hints: Set<CellHint>)
}