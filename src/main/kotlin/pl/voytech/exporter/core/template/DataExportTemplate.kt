package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.Hint
import pl.voytech.exporter.core.model.hints.RowHint
import java.io.OutputStream

open class DataExportTemplate<T>(private val delegate: ExportOperations<T>) {

    internal data class MergedRow<T>(val matchingRows: List<Row<T>>?){
        val rowHints: Set<RowHint>?
        val rowCellHints: Set<CellHint>?
        val rowCells: Map<String,Cell<T>>?

        init {
            rowHints = collectHints(matchingRows) { r -> r.rowHints }
            rowCellHints = collectHints(matchingRows) { r -> r.cellHints }
            rowCells = collectCells(matchingRows)
        }

        private fun <E : Hint> collectHints(matchingRows: List<Row<T>>?, getHints:(r: Row<T>) -> Set<E>?): Set<E>? {
            return matchingRows?.mapNotNull { e -> getHints.invoke(e) }
                ?.fold(setOf(),{ acc, r -> acc + r })
        }

        private fun collectCells(matchingRows: List<Row<T>>?): Map<String, Cell<T>>? {
            return matchingRows?.mapNotNull { row -> row.cells }
                ?.fold(mapOf(),{ acc, m -> acc + m })
        }
    }

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
            renderColumnsTitlesRow(state, table, collection)
        }
        val startFrom = state.rowIndex
        collection.forEachIndexed { rowIndex: Int, record: T ->
            exportRow(state, table, record, collection, rowIndex.plus(startFrom))
        }
        return state
    }

    private fun renderColumnsTitlesRow(state: ExportingState, table: Table<T>, collection: Collection<T>): ExportingState {
        val headerRowMeta = collectMatchingRowDefinitions(RowData(index = state.rowIndex,dataset = collection), table.rows)
        return delegate.renderColumnsTitlesRow(state.delegate, state.coordinates(), headerRowMeta.rowHints).let {
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

    private fun exportRow(state: ExportingState, table: Table<T>, record: T, collection: Collection<T>, rowIndex: Int) {
        RowData(rowIndex, record, collection).let { row ->
            val rowMeta = collectMatchingRowDefinitions(row, table.rows)
            renderRow(state.nextRowIndex(rowIndex), rowMeta.rowHints).also {
                table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                    val cellDef = rowMeta.rowCells?.get(column.id)
                    val value = cellDef?.eval?.invoke(row) ?: cellDef?.value ?: column.fromField?.invoke(record)
                    renderRowCell(
                        it.nextColumnIndex(column.index ?: columnIndex),
                        value?.let { CellValue(value, cellDef?.type ?: column.columnType) },
                        collectUniqueCellHints(table.cellHints, column.cellHints, rowMeta.rowCellHints, cellDef?.cellHints)
                    )
                }
            }
        }
    }

    private fun collectMatchingRowDefinitions(row: RowData<T>, tableRows: List<Row<T>>?): MergedRow<T> = MergedRow(tableRows?.filter { it.selector(row) })

    private fun collectUniqueCellHints(vararg hintsOnLevels: Set<CellHint>?): Set<CellHint> {
        return hintsOnLevels.filterNotNull().fold(setOf(), {acc, s -> acc + s })
    }

}