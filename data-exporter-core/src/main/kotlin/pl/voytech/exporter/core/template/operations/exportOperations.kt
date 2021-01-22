package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.FileData
import pl.voytech.exporter.core.template.context.ColumnOperationTableData
import pl.voytech.exporter.core.template.context.OperationContext
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
        context: OperationContext<ColumnOperationTableData>
    )
}

interface RowOperation<T, A> {
    fun renderRow(
        state: A,
        context: OperationContext<AttributedRow<T>>
    )
}

interface RowCellOperation<T, A> {
    fun renderRowCell(
        state: A,
        context: OperationContext<AttributedCell>
    )
}

interface TableOperations<T, A> : CreateTableOperation<T, A>, ColumnOperation<T, A>, RowOperation<T, A>,
    RowCellOperation<T, A>

class ExportOperations<T, A>(
    val lifecycleOperations: LifecycleOperations<A>,
    val tableOperations: TableOperations<T, A>
)
