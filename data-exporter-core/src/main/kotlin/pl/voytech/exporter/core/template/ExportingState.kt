package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Key
import pl.voytech.exporter.core.model.NextId
import pl.voytech.exporter.core.model.TypedRowData

/**
 * A mutable exporting state representing entire dataset as well as operation-scoped context data and coordinates for
 * operation execution.
 * @author Wojciech Mąka
 */
class ExportingState<T, A>(
    val delegate: DelegateAPI<A>,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
    val collection: Collection<T>
) {
    /**
     * Instance of mutable context for row-scope operations. After changing coordinate denoting advancing the row,
     * coordinate object is recreated, and new row associated context data is being set. Then instance is used on all
     * kind of given row scoped operations.
     */
    private val rowOperationContext: OperationContext<T, RowOperationTableDataContext<T>> =
        OperationContext(RowOperationTableDataContext(collection))
    /**
     * Instance of mutable context for cell-scope operations. After changing coordinate denoting advancing the cell,
     * coordinate object is recreated, and new cell associated context data is being set. Then instance is used on all
     * kind of given cell scoped operations.
     */
    private val cellOperationContext: OperationContext<T, CellOperationTableDataContext<T>> =
        OperationContext(CellOperationTableDataContext(collection))
    /**
     * Instance of mutable context for column-scope operations. After changing coordinate denoting advancing the column,
     * coordinate object is recreated, and new column associated context data is being set. Then instance is used on all
     * kind of given column scoped operations.
     */
    private val columnOperationContext: OperationContext<T, ColumnOperationTableDataContext<T>> =
        OperationContext(ColumnOperationTableDataContext(collection))


    internal val rowValues : MutableList<DataExportTemplate.ComputedRowValue<T>> = mutableListOf()

    private var rowValue: DataExportTemplate.ComputedRowValue<T>? = null

    /**
     * rowIndex is modified for row row change. It is used for recreation of unmodifiable Coordinates object.
     */
    var rowIndex: Int = 0 //objectIndex
    /**
     * columnIndex is modified for column change. It is used for recreation of unmodifiable Coordinates object.
     */
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

    private fun withCurrentRowValue(rowValue: DataExportTemplate.ComputedRowValue<T>): ExportingState<T, A> {
        this.rowValue = rowValue
        return this
    }

    internal fun addRow(rowValue: DataExportTemplate.ComputedRowValue<T>): ExportingState<T, A>  {
        rowValues.add(rowValue)
        nextRowIndex()
        return this
    }

    fun forEachRowValue(block: (rowValue: DataExportTemplate.ComputedRowValue<T>) -> Unit): ExportingState<T, A> {
        withRowIndex(0)
        rowValues.forEachIndexed { index, rowValue ->
            withRowIndex(index)
            withColumnIndex(0)
            withCurrentRowValue(rowValue)
            block.invoke(rowValue)
        }
        return this
    }

    fun <T> rowData(dataset: Collection<T>, record: T? = null, objectIndex: Int? = null) =
        TypedRowData(rowIndex, objectIndex, record, dataset)

    fun rowOperationContext(): OperationContext<T, RowOperationTableDataContext<T>> {
        rowOperationContext.coordinates = coordinates()
        rowOperationContext.data.rowValues = rowValue?.rowCellValues
        return rowOperationContext
    }

    fun cellOperationContext(columnIndex: Int, columnId: Key<T>): OperationContext<T, CellOperationTableDataContext<T>> {
        return withColumnIndex(columnIndex).let {
            cellOperationContext.coordinates = coordinates()
            cellOperationContext.data.cellValue = rowValue?.rowCellValues?.get(columnId)
            cellOperationContext
        }
    }

    fun columnOperationContext(columnIndex: Int, columnId: Key<T>): OperationContext<T, ColumnOperationTableDataContext<T>> {
        return withColumnIndex(columnIndex).let {
            columnOperationContext.coordinates = coordinates()
            columnOperationContext.data.columnValues = rowValues.map { v -> v.rowCellValues[columnId]!! }
            columnOperationContext
        }
    }

    private fun coordinates(): Coordinates =
        Coordinates(tableName, (firstRow ?: 0) + rowIndex, (firstColumn ?: 0) + columnIndex)

}