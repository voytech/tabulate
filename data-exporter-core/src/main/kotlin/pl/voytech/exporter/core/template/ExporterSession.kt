package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Column
import pl.voytech.exporter.core.model.NextId
import pl.voytech.exporter.core.model.Table

/**
 * A mutable exporting state representing entire dataset as well as operation-scoped context data and coordinates for
 * operation execution.
 * @author Wojciech Mąka
 */
class ExporterSession<T, A>(
    val delegate: A,
    val tableModel: Table<T>,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
    val collection: Collection<T>
) {
    private val stateAttributes = mutableMapOf<String, Any>()

    /**
     * Instance of mutable context for row-scope operations. After changing coordinate denoting advancing the row,
     * coordinate object is recreated, and new row associated context data is being set. Then instance is used on all
     * kind of given row scoped operations.
     */
    private val rowContext: OperationContext<AttributedRow<T>> =
        OperationContext(stateAttributes)

    /**
     * Instance of mutable context for cell-scope operations. After changing coordinate denoting advancing the cell,
     * coordinate object is recreated, and new cell associated context data is being set. Then instance is used on all
     * kind of given cell scoped operations.
     */
    private val cellContext: OperationContext<AttributedCell> =
        OperationContext(stateAttributes)

    /**
     * Instance of mutable context for column-scope operations. After changing coordinate denoting advancing the column,
     * coordinate object is recreated, and new column associated context data is being set. Then instance is used on all
     * kind of given column scoped operations.
     */
    private val columnContext: OperationContext<ColumnOperationTableData> =
        OperationContext<ColumnOperationTableData>(stateAttributes).also { it.data = ColumnOperationTableData()}

    private val rowValues: MutableList<AttributedRow<T>> = mutableListOf()

    private val coordinates: Coordinates = Coordinates(tableName)

    init {
        columnContext.coordinates = coordinates
        rowContext.coordinates = coordinates
        cellContext.coordinates = coordinates
    }

    internal fun addRow(rowValue: AttributedRow<T>): ExporterSession<T, A> {
        rowValues.add(rowValue)
        return this
    }

    internal fun forEachRowValue(block: (context: OperationContext<AttributedRow<T>>) -> Unit): ExporterSession<T, A> {
        rowValues.forEachIndexed { rowIndex, rowValue ->
            block.invoke(setRowContext(rowValue, rowIndex))
        }
        return this
    }

    private fun setRowContext(row: AttributedRow<T>, rowIndex: Int): OperationContext<AttributedRow<T>> {
        return with(rowContext) {
            coordinates.rowIndex = (firstRow ?: 0) + rowIndex
            coordinates.columnIndex = 0
            data = row
            this
        }
    }

    internal fun setCellContext(
        columnIndex: Int,
        cell: AttributedCell
    ): OperationContext<AttributedCell> {
        return with(cellContext) {
            coordinates.columnIndex = (firstColumn ?: 0) + columnIndex
            data = cell
            this
        }
    }

    internal fun setColumnContext(
        columnIndex: Int,
        column: Column<T>,
        phase: ColumnRenderPhase
    ): OperationContext<ColumnOperationTableData> {
        return with(columnContext) {
            coordinates.columnIndex = (firstColumn ?: 0) + columnIndex
            data!!.currentPhase = phase
            data!!.columnValues = rowValues.mapNotNull { v -> v.rowCellValues[column.id]?.value }
            data!!.columnAttributes = column.columnAttributes?.filter { ext ->
                ((ColumnRenderPhase.BEFORE_FIRST_ROW == phase) && ext.beforeFirstRow()) ||
                        ((ColumnRenderPhase.AFTER_LAST_ROW == phase) && ext.afterLastRow())
            }?.toSet()
            this
        }
    }

}
