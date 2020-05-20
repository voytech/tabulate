package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint
import java.io.OutputStream

interface ExportOperations<T> {
    fun init(table: Table<T>): DelegateState
    fun renderColumnsTitlesRow(state: DelegateState, coordinates: Coordinates)
    fun renderColumn(state: DelegateState,columnIndex: Int, columnHints: Set<ColumnHint>?)
    fun renderColumnTitleCell(state: DelegateState, coordinates: Coordinates, columnTitle: Description?, cellHints: Set<CellHint>?)
    fun renderRow(state: DelegateState, coordinates: Coordinates, rowHints: Set<RowHint>?)
    fun renderRowCell(state: DelegateState, coordinates: Coordinates, value: CellValue?, cellHints: Set<CellHint>?)
    fun complete(state: DelegateState, coordinates: Coordinates): FileData<ByteArray>
    fun complete(state: DelegateState, coordinates: Coordinates, stream: OutputStream)
}