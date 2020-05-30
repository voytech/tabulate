package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import java.io.OutputStream

interface RowOperation<A> {
    fun renderRow(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<RowExtension>?)
}

interface CreateDocumentOperation<A> {
    fun createDocument(): DelegateAPI<A>
}

interface CreateTableOperation<T,A> {
    fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>
}

interface FinishDocumentOperations<A> {
    fun finishDocument(state: DelegateAPI<A>): FileData<ByteArray>
    fun finishDocument(state: DelegateAPI<A>, stream: OutputStream)
}

interface ColumnOperation<A> {
    fun renderColumn(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<ColumnExtension>?)
}

interface HeaderCellOperation<A> {
    fun renderHeaderCell(state: DelegateAPI<A>, coordinates: Coordinates, columnTitle: Description?, extensions: Set<CellExtension>?)
}

interface RowCellOperation<A> {
    fun renderRowCell(state: DelegateAPI<A>, coordinates: Coordinates, value: CellValue?, extensions: Set<CellExtension>?)
}

data class LifecycleOperations<T,A>(
    val createDocumentOperation: CreateDocumentOperation<A>,
    val createTableOperation: CreateTableOperation<T,A>,
    val finishDocumentOperations: FinishDocumentOperations<A>
)

data class ExportOperations<T,A>(
    val lifecycleOperations: LifecycleOperations<T,A>,
    val headerRowOperation: RowOperation<A>? = null,
    val rowOperation: RowOperation<A>,
    val columnOperation: ColumnOperation<A>? = null,
    val headerCellOperation: HeaderCellOperation<A>? = null,
    val rowCellOperation: RowCellOperation<A>? = null
)
