package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.*

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
    private val rowSkips = mutableMapOf<ColumnKey<T>, Int>()
    private var colSkips = 0

    var currentRowIndex = 0
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
        OperationContext<ColumnOperationTableData>(stateAttributes).also { it.data = ColumnOperationTableData() }


    private val coordinates: Coordinates = Coordinates(tableName)

    init {
        columnContext.coordinates = coordinates
        rowContext.coordinates = coordinates
        cellContext.coordinates = coordinates
    }

    internal fun getRowContextAndAdvance(row: AttributedRow<T>): OperationContext<AttributedRow<T>> {
        return with(rowContext) {
            coordinates.rowIndex = (firstRow ?: 0) + currentRowIndex++
            coordinates.columnIndex = 0
            data = row
            this
        }
    }

    internal fun getCellContext(columnIndex: Int, column: Column<T>): OperationContext<AttributedCell> {
        return rowContext.data.let { attributedRow ->
            with(cellContext) {
                coordinates.columnIndex = (firstColumn ?: 0) + columnIndex
                data = attributedRow?.rowCellValues?.get(column.id) ?: error("")
                this
            }
        }
    }

    internal fun getColumnContext(
        columnIndex: Int,
        column: Column<T>,
        phase: ColumnRenderPhase
    ): OperationContext<ColumnOperationTableData> {
        return with(columnContext) {
            coordinates.columnIndex = (firstColumn ?: 0) + columnIndex
            data!!.currentPhase = phase
            data!!.columnAttributes = column.columnAttributes?.filter { ext ->
                ((ColumnRenderPhase.BEFORE_FIRST_ROW == phase) && ext.beforeFirstRow()) ||
                        ((ColumnRenderPhase.AFTER_LAST_ROW == phase) && ext.afterLastRow())
            }?.toSet()
            this
        }
    }

    internal fun createSourceRow(objectIndex: Int? = null, record: T? = null): SourceRow<T> =
        SourceRow(dataset = collection, rowIndex = currentRowIndex, objectIndex = objectIndex, record = record)

    internal fun determineRowSkips(column: Column<T>, cell: Cell<T>?) {
        colSkips = (cell?.colSpan?.minus(1)) ?: 0
        rowSkips[column.id] = (cell?.rowSpan?.minus(1)) ?: 0
    }

    internal fun noSkip(column: Column<T>): Boolean {
        return colSkips-- <= 0 && (rowSkips[column.id] ?: 0).also { rowSkips[column.id] = it - 1 } <= 0
    }
}
