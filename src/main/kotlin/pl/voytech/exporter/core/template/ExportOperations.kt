package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import java.io.OutputStream

interface RowOperation {
    fun renderRow(state: DelegateAPI, coordinates: Coordinates, extensions: Set<RowExtension>?)
}

interface CreateDocumentOperation {
    fun createDocument(): DelegateAPI
}

interface CreateTableOperation<T> {
    fun createTable(state: DelegateAPI, table: Table<T>): DelegateAPI
}

interface FinishDocumentOperations {
    fun finishDocument(state: DelegateAPI): FileData<ByteArray>
    fun finishDocument(state: DelegateAPI, stream: OutputStream)
}

interface ColumnOperation {
    fun renderColumn(state: DelegateAPI, coordinates: Coordinates, extensions: Set<ColumnExtension>?)
}

interface HeaderCellOperation {
    fun renderHeaderCell(state: DelegateAPI, coordinates: Coordinates, columnTitle: Description?, extensions: Set<CellExtension>?)
}

interface RowCellOperation {
    fun renderRowCell(state: DelegateAPI, coordinates: Coordinates, value: CellValue?, extensions: Set<CellExtension>?)
}

data class LifecycleOperations<T>(
    val createDocumentOperation: CreateDocumentOperation,
    val createTableOperation: CreateTableOperation<T>,
    val finishDocumentOperations: FinishDocumentOperations
)

data class ExportOperations<T>(
    val lifecycleOperations: LifecycleOperations<T>,
    val headerRowOperation: RowOperation? = null,
    val rowOperation: RowOperation,
    val columnOperation: ColumnOperation? = null,
    val headerCellOperation: HeaderCellOperation? = null,
    val rowCellOperation: RowCellOperation? = null
)
