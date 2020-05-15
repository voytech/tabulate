package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Column
import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.RowData
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
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
                 renderColumnTitleCell(
                    state.nextColumnIndex(columnIndex),
                    column.columnTitle,
                    normalizeCellHints(table.cellHints, column.cellHints)
                )
            }
        }.let { state.nextRowIndex() }
    }

    private fun renderColumnTitleCell(state: ExportingState, columnTitle: Description?, cellHints: List<CellHint>?): ExportingState {
        return columnTitle?.let { delegate.renderColumnTitleCell(state.delegate, state.coordinates(), columnTitle, cellHints); state } ?: state
    }

    private fun renderRow(state: ExportingState, rowHints: List<RowHint>?): ExportingState {
        return delegate.renderRow(state.delegate, state.coordinates(), rowHints).let { state }
    }

    private fun renderRowCell(state: ExportingState, value: CellValue?, cellHints: List<CellHint>?) : ExportingState {
        return delegate.renderRowCell(state.delegate, state.coordinates(), value, cellHints).let { state }
    }

    private fun exportRow(state: ExportingState, table: Table<T>, record: T, collection: Collection<T>, rowIndex: Int) {
        RowData(rowIndex, record, collection).let { row ->
            val rowDef = table.rows?.find { it.selector(row) }
            val rowHints = rowDef?.rowHints
            val cells = rowDef?.cells
            renderRow(state.nextRowIndex(rowIndex), rowHints).also {
                table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                    val cellDef = cells?.get(column.id)
                    val value = cellDef?.eval?.invoke(row) ?: cellDef?.value ?: column.fromField?.invoke(record)
                    renderRowCell(
                        it.nextColumnIndex(columnIndex),
                        value?.let { CellValue(value, cellDef?.type ?: column.columnType) },
                        normalizeCellHints(table.cellHints, column.cellHints, rowDef?.cellHints, cellDef?.cellHints)
                    )
                }
            }
        }
    }

    private fun normalizeCellHints(vararg hintsOnLevels: List<CellHint>?): List<CellHint> {
        return listOf()
    }

}