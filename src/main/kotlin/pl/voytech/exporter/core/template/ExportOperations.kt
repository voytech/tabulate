package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Column
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.RowHint

interface ExportOperations<T> {
    fun init(): DelegateState
    fun renderColumnsHeaders(state: ExportingState, columns: List<Column<T>>): ExportingState
    fun renderRow(state: ExportingState, rowHints: List<RowHint>?): ExportingState
    fun renderRowCell(state: ExportingState, value: CellValue?, cellHints: List<CellHint>?) : ExportingState

}