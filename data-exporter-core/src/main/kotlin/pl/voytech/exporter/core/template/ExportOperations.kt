package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import java.io.OutputStream


interface RowOperation<T, A> {
    fun renderRow(state: DelegateAPI<A>, context: OperationContext<T,RowOperationTableDataContext<T>>, extensions: Set<RowExtension>?)
}

interface CreateDocumentOperation<A> {
    fun createDocument(): DelegateAPI<A>
}

interface CreateTableOperation<T, A> {
    fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>
}

interface FinishDocumentOperations<A> {
    fun finishDocument(state: DelegateAPI<A>): FileData<ByteArray>
    fun finishDocument(state: DelegateAPI<A>, stream: OutputStream)
}

interface ColumnOperation<T, A> {
    fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableDataContext<T>>,
        extensions: Set<ColumnExtension>?
    )
}

interface RowCellOperation<T, A> {
    fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableDataContext<T>>,
        extensions: Set<CellExtension>?
    )
}

data class ExportOperations<T, A>(
    val createDocumentOperation: CreateDocumentOperation<A>,
    val createTableOperation: CreateTableOperation<T, A>,
    val finishDocumentOperations: FinishDocumentOperations<A>,
    val rowOperation: RowOperation<T, A>,
    val columnOperation: ColumnOperation<T, A>? = null,
    val rowCellOperation: RowCellOperation<T, A>? = null
)
