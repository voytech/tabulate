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

    private val rowValues : MutableList<DataExportTemplate.ComputedRowValue<T>> = mutableListOf()

    private var rowValue: DataExportTemplate.ComputedRowValue<T>? = null

    /**
     * rowIndex is modified for row row change. It is used for recreation of unmodifiable Coordinates object.
     */
    private var rowIndex: Int = 0 //objectIndex
    /**
     * columnIndex is modified for column change. It is used for recreation of unmodifiable Coordinates object.
     */
    private var columnIndex: Int = 0 //objectFieldIndex

    internal fun addRow(rowValue: DataExportTemplate.ComputedRowValue<T>): ExportingState<T, A>  {
        rowValues.add(rowValue)
        return this
    }

    internal fun forEachRowValue(block: (rowValue: DataExportTemplate.ComputedRowValue<T>) -> Unit): ExportingState<T, A> {
        rowIndex = 0
        rowValues.forEachIndexed { index, rowValue ->
            rowIndex = index
            columnIndex = 0
            this.rowValue = rowValue
            block.invoke(rowValue)
        }
        return this
    }

    internal fun rowOperationContext(): OperationContext<T, RowOperationTableDataContext<T>> {
        rowOperationContext.coordinates = coordinates()
        rowOperationContext.data.rowValues = rowValue?.rowCellValues
        return rowOperationContext
    }

    internal fun cellOperationContext(columnIndex: Int, columnId: Key<T>): OperationContext<T, CellOperationTableDataContext<T>> {
        this.columnIndex = columnIndex
        cellOperationContext.coordinates = coordinates()
        cellOperationContext.data.cellValue = rowValue?.rowCellValues?.get(columnId)
        return cellOperationContext
    }

    internal fun columnOperationContext(columnIndex: Int, columnId: Key<T>): OperationContext<T, ColumnOperationTableDataContext<T>> {
        this.columnIndex = columnIndex
        columnOperationContext.coordinates = coordinates()
        columnOperationContext.data.columnValues = rowValues.mapNotNull { v -> v.rowCellValues[columnId] }
        return columnOperationContext
    }

    private fun coordinates(): Coordinates =
        Coordinates(tableName, (firstRow ?: 0) + rowIndex, (firstColumn ?: 0) + columnIndex)

}