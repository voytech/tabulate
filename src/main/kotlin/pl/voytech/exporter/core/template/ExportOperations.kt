package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint
import java.io.OutputStream

interface RowOperations {
    fun renderHeaderRow(state: DelegateAPI, coordinates: Coordinates, rowHints: Set<RowHint>?)
    fun renderRow(state: DelegateAPI, coordinates: Coordinates, rowHints: Set<RowHint>?)
}

interface LifecycleOperations<T> {
    fun create(): DelegateAPI
    fun init(state: DelegateAPI, table: Table<T>): DelegateAPI
    fun complete(state: DelegateAPI): FileData<ByteArray>
    fun complete(state: DelegateAPI, stream: OutputStream)
}

interface ColumnOperation {
    fun renderColumn(state: DelegateAPI, coordinates: Coordinates, columnHints: Set<ColumnHint>?)
}

interface HeaderCellOperation {
    fun renderHeaderCell(state: DelegateAPI, coordinates: Coordinates, columnTitle: Description?, cellHints: Set<CellHint>?)
}

interface RowCellOperation {
    fun renderRowCell(state: DelegateAPI, coordinates: Coordinates, value: CellValue?, cellHints: Set<CellHint>?)
}

data class ExportOperations<T>(
    val lifecycleOperations: LifecycleOperations<T>,
    val rowOperations: RowOperations,
    val columnOperation: ColumnOperation? = null,
    val headerCellOperation: HeaderCellOperation? = null,
    val rowCellOperation: RowCellOperation? = null
)
