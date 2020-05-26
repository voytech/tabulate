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
        val rowCells: Map<Key<T>,Cell<T>>?

        init {
            rowHints = collectHints(matchingRows) { r -> r.rowHints }
            rowCellHints = collectHints(matchingRows) { r -> r.cellHints }
            rowCells = collectCells(matchingRows)
        }

        private fun <E : Hint> collectHints(matchingRows: List<Row<T>>?, getHints:(r: Row<T>) -> Set<E>?): Set<E>? {
            return matchingRows?.mapNotNull { e -> getHints.invoke(e) }
                ?.fold(setOf(),{ acc, r -> acc + r })
        }

        private fun collectCells(matchingRows: List<Row<T>>?): Map<Key<T>, Cell<T>>?{
            return matchingRows?.mapNotNull { row -> row.cells }
                ?.fold(mapOf(),{ acc, m -> acc + m })
        }
    }

    fun create(): DelegateAPI {
        return delegate.lifecycleOperations.create()
    }

    fun add(state: DelegateAPI, table: Table<T>, collection: Collection<T>): DelegateAPI {
        val exportingState = ExportingState(delegate.lifecycleOperations.init(state, table).let { state }, table.name ?: "table-${NextId.nextId()}")
        if (table.showHeader == true || table.columns.any { it.columnTitle != null }) {
            renderHeaderRow(exportingState, table, collection)
        }
        val startFrom = exportingState.rowIndex
        collection.forEachIndexed { rowIndex: Int, record: T ->
            exportRow(exportingState, table, record, collection, rowIndex.plus(startFrom))
        }
        return exportingState.delegate
    }

    fun export(table: Table<T>, collection: Collection<T>): FileData<ByteArray> {
        return add(create(), table, collection).let { delegate.lifecycleOperations.complete(it) }
    }

    fun export(table: Table<T>, collection: Collection<T>, stream: OutputStream) {
        add(create(), table, collection).also { delegate.lifecycleOperations.complete(it, stream) }
    }

    fun export(state: DelegateAPI, stream: OutputStream) {
        delegate.lifecycleOperations.complete(state, stream)
    }

    private fun renderHeaderRow(state: ExportingState, table: Table<T>, collection: Collection<T>): ExportingState {
        val headerRowMeta = collectMatchingRowDefinitions(RowData(index = state.rowIndex,dataset = collection), table.rows)
        return delegate.rowOperations.renderHeaderRow(state.delegate, state.coordinates(), headerRowMeta.rowHints).let {
            table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                delegate.columnOperation?.renderColumn(state.delegate, state.nextColumnIndex(column.index ?: columnIndex).coordinates(), column.columnHints)
                val cellDef = headerRowMeta.rowCells?.get(column.id)
                renderHeaderCell(
                    state,
                    column.columnTitle,
                    collectUniqueCellHints(table.cellHints, column.cellHints, headerRowMeta.rowCellHints, cellDef?.cellHints)
                )
            }
        }.let { state.nextRowIndex() }
    }

    private fun renderHeaderCell(state: ExportingState, columnTitle: Description?, cellHints: Set<CellHint>?): ExportingState {
        return columnTitle?.let { delegate.headerCellOperation?.renderHeaderCell(state.delegate, state.coordinates(), columnTitle, cellHints); state } ?: state
    }

    private fun renderRow(state: ExportingState, rowHints: Set<RowHint>?): ExportingState {
        return delegate.rowOperations.renderRow(state.delegate, state.coordinates(), rowHints).let { state }
    }

    private fun renderRowCell(state: ExportingState, value: CellValue?, cellHints: Set<CellHint>?) : ExportingState {
        return delegate.rowCellOperation?.renderRowCell(state.delegate, state.coordinates(), value, cellHints).let { state }
    }

    private fun exportRow(state: ExportingState, table: Table<T>, record: T, collection: Collection<T>, rowIndex: Int) {
        RowData(rowIndex, record, collection).let { row ->
            val rowMeta = collectMatchingRowDefinitions(row, table.rows)
            renderRow(state.nextRowIndex(rowIndex), rowMeta.rowHints).also {
                table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                    val cellDef = rowMeta.rowCells?.get(column.id)
                    val value = (cellDef?.eval?.invoke(row) ?: cellDef?.value ?: column.id.ref?.invoke(record))?.let {
                        column.dataFormatter?.invoke(it) ?: it
                    }
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

fun <T> Collection<T>.exportTo(table: Table<T>, delegate: ExportOperations<T>, stream: OutputStream) {
    DataExportTemplate(delegate).export(table,this,stream)
}
