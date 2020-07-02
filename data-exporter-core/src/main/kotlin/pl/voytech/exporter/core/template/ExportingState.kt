package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.NextId
import pl.voytech.exporter.core.model.RowData

/**
 * A state of single table export.
 * @author Wojciech Mąka
 */
class ExportingState<T, A>(
    val delegate: DelegateAPI<A>,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
    val collection: Collection<T>
) {
    private val rowOperationContext: OperationContext<T, RowOperationTableDataContext<T>> =
        OperationContext(RowOperationTableDataContext(collection))
    private val cellOperationContext: OperationContext<T, CellOperationTableDataContext<T>> =
        OperationContext(CellOperationTableDataContext(collection))
    private val columnOperationContext: OperationContext<T, ColumnOperationTableDataContext<T>> =
        OperationContext(ColumnOperationTableDataContext(collection))

    var rowIndex: Int = 0 //objectIndex
    var columnIndex: Int = 0 //objectFieldIndex

    fun withColumnIndex(index: Int): ExportingState<T, A> {
        columnIndex = index
        return this
    }

    fun withRowIndex(index: Int): ExportingState<T, A> {
        rowIndex = index
        return this
    }

    fun nextRowIndex(): ExportingState<T, A> {
        rowIndex++
        return this
    }

    fun setCurrentRecord(record: T) {
        rowOperationContext.data.record = record
    }

    fun setCurrentFieldValue(propertyValue : Any) {
        cellOperationContext.data.propertyValue = propertyValue
    }

    fun <T> rowData(dataset: Collection<T>, record: T? = null, objectIndex: Int? = null) =
        RowData(rowIndex, objectIndex, record, dataset)

    fun rowOperationContext(): OperationContext<T, RowOperationTableDataContext<T>> {
        rowOperationContext.coordinates = coordinates()
        return rowOperationContext
    }

    fun cellOperationContext(): OperationContext<T, CellOperationTableDataContext<T>> {
        cellOperationContext.coordinates = coordinates()
        return cellOperationContext
    }

    fun columnOperationContext(): OperationContext<T, ColumnOperationTableDataContext<T>> {
        columnOperationContext.coordinates = coordinates()
        return columnOperationContext
    }

    private fun coordinates(): Coordinates =
        Coordinates(tableName, (firstRow ?: 0) + rowIndex, (firstColumn ?: 0) + columnIndex)

}