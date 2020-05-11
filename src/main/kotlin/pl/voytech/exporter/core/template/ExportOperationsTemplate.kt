package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Column
import pl.voytech.exporter.core.model.RowData
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.RowHint

open class ExportOperationsTemplate<T>(private val delegate: ExportOperations<T>) {

    private fun init(): ExportingState {
        return ExportingState(delegate.init())
    }

    fun renderColumnsHeaders(state: ExportingState, table: Table<T>): ExportingState {
        return state
    }

    fun export(table: Table<T>, collection: Collection<T>) {
        val state: ExportingState = init()
        collection.forEachIndexed { rowIndex: Int, record: T ->
            this.exportRow(state, table, record, collection, rowIndex)
        }
    }

    private fun renderRow(state: ExportingState, rowHints: List<RowHint>?): ExportingState {
        return delegate.renderRow(state, rowHints)
    }

    private fun renderRowCell(state: ExportingState, value: CellValue?, cellHints: List<CellHint>?) : ExportingState {
        return delegate.renderRowCell(state,value,cellHints)
    }

    private fun exportRow(state: ExportingState, table: Table<T>, record: T, collection: Collection<T>, rowIndex: Int) {
        val rowDef = table.rows?.find { it.selector(RowData(rowIndex,record,collection)) }
        val rowHints = rowDef?.rowHints
        val cells = rowDef?.cells
        renderRow(state.nextRowIndex(rowIndex), rowHints).also {
            table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                val cellDef = cells?.get(column.id)
                val value = column.fromField?.invoke(record)
                renderRowCell(
                    it.nextColumnIndex(columnIndex),
                    if (value!=null) CellValue(value, column.columnType) else null,
                    normalizeCellHints(listOf(column.cellHints, cellDef?.cellHints))
                )
            }
        }
    }

    private fun normalizeCellHints(hints: List<List<CellHint>?>): List<CellHint> {
        return listOf()
    }

    fun collect() {

    }
}