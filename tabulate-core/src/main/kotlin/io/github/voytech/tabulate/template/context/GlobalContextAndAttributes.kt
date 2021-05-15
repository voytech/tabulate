package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute

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

    init {
        stateAttributes["_tableId"] = tableName
    }

    internal fun createRowContext(relativeRowIndex: Int, rowAttributes: Set<RowAttribute>, cells: Map<ColumnKey<T>, AttributedCell>): AttributedRow<T> {
        return AttributedRow(
            rowIndex = (firstRow ?: 0) + relativeRowIndex,
            rowAttributes =  rowAttributes,
            rowCellValues =  cells
        ).apply { additionalAttributes = stateAttributes }
    }

    internal fun createCellContext(
        relativeRowIndex: Int,
        relativeColumnIndex: Int,
        value: CellValue,
        attributes: Set<CellAttribute>
    ): AttributedCell {
        return AttributedCell(
            value = value,
            attributes = attributes,
            rowIndex = (firstRow ?: 0) + relativeRowIndex,
            columnIndex = (firstColumn ?: 0) + relativeColumnIndex,
        ).apply { additionalAttributes = stateAttributes }
    }

    internal fun createColumnContext(
        indexedColumn: IndexedValue<Column<T>>,
        phase: ColumnRenderPhase
    ): AttributedColumn {
        return AttributedColumn(
            columnIndex = (firstColumn ?: 0) + indexedColumn.index,
            currentPhase = phase,
            columnAttributes = indexedColumn.value.columnAttributes?.filter { ext ->
                ((ColumnRenderPhase.BEFORE_FIRST_ROW == phase) && ext.beforeFirstRow()) ||
                        ((ColumnRenderPhase.AFTER_LAST_ROW == phase) && ext.afterLastRow())
            }?.toSet(),
        ).apply { additionalAttributes = stateAttributes }
    }

    internal fun applySpans(column: Column<T>, cell: Cell<T>?) {
        colSkips = (cell?.colSpan?.minus(1)) ?: 0
        rowSkips[column.id] = (cell?.rowSpan?.minus(1)) ?: 0
    }

    internal fun dontSkip(column: Column<T>): Boolean {
        return colSkips-- <= 0 && (rowSkips[column.id] ?: 0).also { rowSkips[column.id] = it - 1 } <= 0
    }
}
