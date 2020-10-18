package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import java.io.OutputStream

interface CreateDocumentOperation<A> {
    fun createDocument(): DelegateAPI<A>
}

interface CreateTableOperation<T, A> {
    fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>
}

interface SaveDocumentOperations<A> {
    fun saveDocument(state: DelegateAPI<A>): FileData<ByteArray>
    fun saveDocument(state: DelegateAPI<A>, stream: OutputStream)
}

interface LifecycleOperations<A> : CreateDocumentOperation<A>, SaveDocumentOperations<A>

interface ColumnOperation<T, A> {
    fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        attributes: Set<ColumnAttribute>?
    )
}

interface RowOperation<T, A> {
    fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        attributes: Set<RowAttribute>?
    )
}

interface RowCellOperation<T, A> {
    fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        attributes: Set<CellAttribute>?
    )
}

interface TableOperations<T, A> : CreateTableOperation<T, A>, ColumnOperation<T, A>, RowOperation<T, A>,
    RowCellOperation<T, A>

class ExportOperations<T, A>(
    val lifecycleOperations: LifecycleOperations<A>,
    val tableOperations: TableOperations<T, A>
)
