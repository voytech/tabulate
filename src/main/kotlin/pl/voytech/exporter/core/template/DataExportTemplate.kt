package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.Hint
import pl.voytech.exporter.core.model.hints.RowHint
import java.io.OutputStream

open class DataExportTemplate<T>(private val delegate: ExportOperations<T>) {

    fun exportToByteArray(table: Table<T>, collection: Collection<T>): FileData<ByteArray> {
        return build(table,collection).let { delegate.complete(it.delegate, it.coordinates()) }
    }

    fun exportToStream(table: Table<T>, collection: Collection<T>, stream: OutputStream) {
        build(table,collection).also { delegate.complete(it.delegate, it.coordinates(),stream) }
    }

    private fun init(table: Table<T>): ExportingState {
        return ExportingState(delegate.init(table))
    }

    private fun build(table: Table<T>, collection: Collection<T>): ExportingState {
        val state: ExportingState = init(table)
        if (table.showHeader == true || table.columns.any { it.columnTitle != null }) {
            renderColumnsTitlesRow(state, table)
        }
        val startFrom = state.rowIndex
        collection.forEachIndexed { rowIndex: Int, record: T ->
            exportRow(state, table, record, collection, rowIndex.plus(startFrom))
        }
        return state
    }

    private fun renderColumnsTitlesRow(state: ExportingState, table: Table<T>): ExportingState {
        return delegate.renderColumnsTitlesRow(state.delegate,state.coordinates()).let {
            table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                 delegate.renderColumn(state.delegate, column.index ?: columnIndex, column.columnHints)
                 renderColumnTitleCell(
                    state.nextColumnIndex(column.index ?: columnIndex),
                    column.columnTitle,
                    collectUniqueCellHints(table.cellHints, column.cellHints)
                )
            }
        }.let { state.nextRowIndex() }
    }

    private fun renderColumnTitleCell(state: ExportingState, columnTitle: Description?, cellHints: Set<CellHint>?): ExportingState {
        return columnTitle?.let { delegate.renderColumnTitleCell(state.delegate, state.coordinates(), columnTitle, cellHints); state } ?: state
    }

    private fun renderRow(state: ExportingState, rowHints: Set<RowHint>?): ExportingState {
        return delegate.renderRow(state.delegate, state.coordinates(), rowHints).let { state }
    }

    private fun renderRowCell(state: ExportingState, value: CellValue?, cellHints: Set<CellHint>?) : ExportingState {
        return delegate.renderRowCell(state.delegate, state.coordinates(), value, cellHints).let { state }
    }

    private fun <E : Hint> collectHints(matchingRows: List<Row<T>>?, getHints:(r: Row<T>) -> Set<E>?): Set<E>? {
        return matchingRows?.mapNotNull { e -> getHints.invoke(e) }
            ?.fold(setOf(),{ acc, r -> acc + r })
    }

    private fun collectCells(matchingRows: List<Row<T>>?): Map<String, Cell<T>>? {
        return matchingRows?.mapNotNull { row -> row.cells }
            ?.fold(mapOf(),{ acc, m -> acc + m })
     }

    private fun exportRow(state: ExportingState, table: Table<T>, record: T, collection: Collection<T>, rowIndex: Int) {
        RowData(rowIndex, record, collection).let { row ->
            val matchingRows = table.rows?.filter { it.selector(row) }
            val rowHints: Set<RowHint>? = collectHints(matchingRows) { r -> r.rowHints }
            val rowDefCellHints: Set<CellHint>? = collectHints(matchingRows) { r -> r.cellHints }
            val cells = collectCells(matchingRows)
            renderRow(state.nextRowIndex(rowIndex), rowHints).also {
                table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                    val cellDef = cells?.get(column.id)
                    val value = cellDef?.eval?.invoke(row) ?: cellDef?.value ?: column.fromField?.invoke(record)
                    renderRowCell(
                        it.nextColumnIndex(column.index ?: columnIndex),
                        value?.let { CellValue(value, cellDef?.type ?: column.columnType) },
                        collectUniqueCellHints(table.cellHints, column.cellHints, rowDefCellHints, cellDef?.cellHints)
                    )
                }
            }
        }
    }

    private fun collectUniqueCellHints(vararg hintsOnLevels: Set<CellHint>?): Set<CellHint> {
        return hintsOnLevels.filterNotNull().fold(setOf(), {acc, s -> acc + s })
    }

}