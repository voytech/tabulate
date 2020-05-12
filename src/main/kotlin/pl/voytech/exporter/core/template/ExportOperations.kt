package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.RowHint
import java.io.OutputStream

interface ExportOperations<T> {
    fun init(table: Table<T>): DelegateState
    fun renderColumnsTitlesRow(state: ExportingState): ExportingState
    fun renderColumnTitleCell(state: ExportingState, columnTitle: Description?, cellHints: List<CellHint>?): ExportingState
    fun renderRow(state: ExportingState, rowHints: List<RowHint>?): ExportingState
    fun renderRowCell(state: ExportingState, value: CellValue?, cellHints: List<CellHint>?): ExportingState
    fun complete(state: ExportingState): FileData<ByteArray>
    fun complete(state: ExportingState, stream: OutputStream)
}