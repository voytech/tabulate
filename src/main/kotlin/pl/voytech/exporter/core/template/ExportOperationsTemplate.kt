package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Column
import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.RowData
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.RowHint
import java.io.OutputStream

open class ExportOperationsTemplate<T>(private val delegate: ExportOperations<T>) {

    fun exportToByteArray(table: Table<T>, collection: Collection<T>): FileData<ByteArray> {
        build(table,collection).also { return delegate.complete(it) }
    }

    fun exportToStream(table: Table<T>, collection: Collection<T>, stream: OutputStream) {
        build(table,collection).also { delegate.complete(it,stream) }
    }

    private fun init(table: Table<T>): ExportingState {
        return ExportingState(delegate.init(table))
    }

    private fun build(table: Table<T>, collection: Collection<T>): ExportingState {
        val state: ExportingState = init(table)
        if (table.showHeader == true && table.columns.any { it.columnTitle != null }) {
            renderColumnsTitlesRow(state, table)
        }
        collection.forEachIndexed { rowIndex: Int, record: T ->
            exportRow(state, table, record, collection, rowIndex)
        }
        return state
    }

    private fun renderColumnsTitlesRow(state: ExportingState, table: Table<T>): ExportingState {
        return delegate.renderColumnsTitlesRow(state).also {
            table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                 renderColumnTitleCell(
                    it.nextColumnIndex(columnIndex),
                    column.columnTitle,
                    normalizeCellHints(table.cellHints, column.cellHints)
                )
            }
        }
    }

    private fun renderColumnTitleCell(state: ExportingState, columnTitle: Description?, cellHints: List<CellHint>?): ExportingState {
        if (columnTitle != null) {
            return delegate.renderColumnTitleCell(state, columnTitle, cellHints)
        }
        return state
    }

    private fun renderRow(state: ExportingState, rowHints: List<RowHint>?): ExportingState {
        return delegate.renderRow(state, rowHints)
    }

    private fun renderRowCell(state: ExportingState, value: CellValue?, cellHints: List<CellHint>?) : ExportingState {
        return delegate.renderRowCell(state, value, cellHints)
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
                    normalizeCellHints(table.cellHints, column.cellHints, rowDef?.cellHints, cellDef?.cellHints)
                )
            }
        }
    }

    private fun normalizeCellHints(vararg hintsOnLevels: List<CellHint>?): List<CellHint> {
        return listOf()
    }

}