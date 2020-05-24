package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint
import java.io.OutputStream

interface BasicOperations<T> {
    fun init(table: Table<T>): DelegateState
    fun renderHeaderRow(state: DelegateState, coordinates: Coordinates, rowHints: Set<RowHint>?)
    fun renderRow(state: DelegateState, coordinates: Coordinates, rowHints: Set<RowHint>?)
    fun complete(state: DelegateState, coordinates: Coordinates): FileData<ByteArray>
    fun complete(state: DelegateState, coordinates: Coordinates, stream: OutputStream)
}


interface ColumnOperation {
    fun renderColumn(state: DelegateState,columnIndex: Int, columnHints: Set<ColumnHint>?)
}
interface HeaderCellOperation {
    fun renderHeaderCell(state: DelegateState, coordinates: Coordinates, columnTitle: Description?, cellHints: Set<CellHint>?)
}

interface RowCellOperation {
    fun renderRowCell(state: DelegateState, coordinates: Coordinates, value: CellValue?, cellHints: Set<CellHint>?)
}

data class ExportOperations<T>(
    val basicOperations: BasicOperations<T>,
    val columnOperation: ColumnOperation? = null,
    val headerCellOperation: HeaderCellOperation? = null,
    val rowCellOperation: RowCellOperation? = null
)
