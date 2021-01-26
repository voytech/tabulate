package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.FileData
import pl.voytech.exporter.core.template.context.*
import java.io.OutputStream

interface CreateDocumentOperation<A> {
    fun createDocument(): A
}

interface CreateTableOperation<T, A> {
    fun createTable(state: A, table: Table<T>): Table<T>
}

interface SaveDocumentOperations<A> {
    fun saveDocument(state: A): FileData<ByteArray>
    fun saveDocument(state: A, stream: OutputStream)
}

interface LifecycleOperations<A> : CreateDocumentOperation<A>, SaveDocumentOperations<A>

interface ColumnOperation<T, A> {
    fun renderColumn(
        state: A,
        context: ColumnOperationContext
    )
}

interface RowOperation<T, A> {
    fun renderRow(
        state: A,
        context: RowOperationContext<T>
    )
}

interface RowCellOperation<T, A> {
    fun renderRowCell(
        state: A,
        context: CellOperationContext
    )
}

interface TableOperations<T, A> : CreateTableOperation<T, A>, ColumnOperation<T, A>, RowOperation<T, A>,
    RowCellOperation<T, A>

class ExportOperations<T, A>(
    val lifecycleOperations: LifecycleOperations<A>,
    val tableOperations: TableOperations<T, A>
)
