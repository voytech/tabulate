package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.Extension
import pl.voytech.exporter.core.model.extension.RowExtension
import java.io.OutputStream

open class DataExportTemplate<T,A>(private val delegate: ExportOperations<T,A>) {

    internal data class MergedRow<T>(val matchingRows: List<Row<T>>?){
        val rowHints: Set<RowExtension>?
        val rowCellHints: Set<CellExtension>?
        val rowCells: Map<Key<T>,Cell<T>>?

        init {
            rowHints = collectHints(matchingRows) { r -> r.rowExtensions }
            rowCellHints = collectHints(matchingRows) { r -> r.cellExtensions }
            rowCells = collectCells(matchingRows)
        }

        private fun <E : Extension> collectHints(matchingRows: List<Row<T>>?, getHints:(r: Row<T>) -> Set<E>?): Set<E>? {
            return matchingRows?.mapNotNull { e -> getHints.invoke(e) }
                ?.fold(setOf(),{ acc, r -> acc + r })
        }

        private fun collectCells(matchingRows: List<Row<T>>?): Map<Key<T>, Cell<T>>?{
            return matchingRows?.mapNotNull { row -> row.cells }
                ?.fold(mapOf(),{ acc, m -> acc + m })
        }
    }

    fun create(): DelegateAPI<A> {
        return delegate.createDocumentOperation.createDocument()
    }

    fun add(state: DelegateAPI<A>, table: Table<T>, collection: Collection<T>): DelegateAPI<A> {
        val exportingState = delegate.createTableOperation.let {
            ExportingState(it.createTable(state,table), table.name ?: "table-${NextId.nextId()}",table.firstRow, table.firstColumn)
        }
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
        return add(create(), table, collection).let { delegate.finishDocumentOperations.finishDocument(it) }
    }

    fun export(table: Table<T>, collection: Collection<T>, stream: OutputStream) {
        add(create(), table, collection).also { delegate.finishDocumentOperations.finishDocument(it, stream) }
    }

    fun export(state: DelegateAPI<A>, stream: OutputStream) {
        delegate.finishDocumentOperations.finishDocument(state, stream)
    }

    private fun renderHeaderRow(state: ExportingState<A>, table: Table<T>, collection: Collection<T>): ExportingState<A> {
        val headerRowMeta = collectMatchingRowDefinitions(RowData(index = state.rowIndex,dataset = collection), table.rows)
        return delegate.rowOperation.renderRow(state.delegate, state.coordinates(), headerRowMeta.rowHints).let {
            table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                delegate.columnOperation?.renderColumn(state.delegate, state.nextColumnIndex(column.index ?: columnIndex).coordinates(), column.columnExtensions)
                val cellDef = headerRowMeta.rowCells?.get(column.id)
                renderHeaderCell(
                    state,
                    column.columnTitle,
                    collectUniqueCellHints(table.cellExtensions, column.cellExtensions, headerRowMeta.rowCellHints, cellDef?.cellExtensions)
                )
            }
        }.let { state.nextRowIndex() }
    }

    private fun renderHeaderCell(state: ExportingState<A>, columnTitle: Description?, cellHints: Set<CellExtension>?): ExportingState<A> {
        return columnTitle?.let { delegate.headerCellOperation?.renderHeaderCell(state.delegate, state.coordinates(), columnTitle, cellHints); state } ?: state
    }

    private fun renderRow(state: ExportingState<A>, rowHints: Set<RowExtension>?): ExportingState<A> {
        return delegate.rowOperation.renderRow(state.delegate, state.coordinates(), rowHints).let { state }
    }

    private fun renderRowCell(state: ExportingState<A>, value: CellValue?, cellHints: Set<CellExtension>?) : ExportingState<A> {
        return delegate.rowCellOperation?.renderRowCell(state.delegate, state.coordinates(), value, cellHints).let { state }
    }

    private fun exportRow(state: ExportingState<A>, table: Table<T>, record: T, collection: Collection<T>, rowIndex: Int) {
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
                        collectUniqueCellHints(table.cellExtensions, column.cellExtensions, rowMeta.rowCellHints, cellDef?.cellExtensions)
                    )
                }
            }
        }
    }

    private fun collectMatchingRowDefinitions(row: RowData<T>, tableRows: List<Row<T>>?): MergedRow<T> = MergedRow(tableRows?.filter { it.selector(row) })

    private fun collectUniqueCellHints(vararg hintsOnLevels: Set<CellExtension>?): Set<CellExtension> {
        return hintsOnLevels.filterNotNull().fold(setOf(), {acc, s -> acc + s })
    }

}

fun <T,A> Collection<T>.exportTo(table: Table<T>, delegate: ExportOperations<T,A>, stream: OutputStream) {
    DataExportTemplate(delegate).export(table, this, stream)
}

