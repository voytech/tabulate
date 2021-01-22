package pl.voytech.exporter.core.template.context

import pl.voytech.exporter.core.model.*

/**
 * A mutable exporting state representing entire dataset as well as operation-scoped context data and coordinates for
 * operation execution.
 * @author Wojciech Mąka
 */
class GlobalContextAndAttributes<T>(
    val tableModel: Table<T>,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0
) {
    private val stateAttributes = mutableMapOf<String, Any>()
    private val rowSkips = mutableMapOf<ColumnKey<T>, Int>()
    private var colSkips = 0

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


    internal fun getRowContext(attributedRow: IndexedValue<AttributedRow<T>>): OperationContext<AttributedRow<T>> {
        return with(rowContext) {
            coordinates.rowIndex = (firstRow ?: 0) + attributedRow.index
            coordinates.columnIndex = 0
            data = attributedRow.value
            this
        }
    }

    internal fun getCellContext(indexedColumn: IndexedValue<Column<T>>): OperationContext<AttributedCell> {
        return rowContext.data.let { attributedRow ->
            with(cellContext) {
                coordinates.columnIndex = (firstColumn ?: 0) + indexedColumn.index
                data = attributedRow?.rowCellValues?.get(indexedColumn.value.id) ?: error("")
                this
            }
        }
    }

    internal fun getColumnContext(
        indexedColumn: IndexedValue<Column<T>>,
        phase: ColumnRenderPhase
    ): OperationContext<ColumnOperationTableData> {
        return with(columnContext) {
            coordinates.columnIndex = (firstColumn ?: 0) + indexedColumn.index
            data!!.currentPhase = phase
            data!!.columnAttributes = indexedColumn.value.columnAttributes?.filter { ext ->
                ((ColumnRenderPhase.BEFORE_FIRST_ROW == phase) && ext.beforeFirstRow()) ||
                        ((ColumnRenderPhase.AFTER_LAST_ROW == phase) && ext.afterLastRow())
            }?.toSet()
            this
        }
    }

    internal fun applySpans(column: Column<T>, cell: Cell<T>?) {
        colSkips = (cell?.colSpan?.minus(1)) ?: 0
        rowSkips[column.id] = (cell?.rowSpan?.minus(1)) ?: 0
    }

    internal fun dontSkip(column: Column<T>): Boolean {
        return colSkips-- <= 0 && (rowSkips[column.id] ?: 0).also { rowSkips[column.id] = it - 1 } <= 0
    }
}
