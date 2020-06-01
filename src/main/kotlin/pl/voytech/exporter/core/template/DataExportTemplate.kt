package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.Extension
import pl.voytech.exporter.core.model.extension.RowExtension
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger

/**
 * Core logic responsible for orchestrating rendering tabular data format file.
 * Takes delegate object with bunch of specialised interfaces. Each interface defines contract for
 * single atomic step of data export.
 * Implementations of interfaces must agree on delegate state or low level api type instance.
 * @author Wojciech Mąka
 */
open class DataExportTemplate<T, A>(private val delegate: ExportOperations<T, A>) {

    internal data class MergedRow<T>(val matchingRows: Set<Row<T>>?) {
        val rowHints: Set<RowExtension>?
        val rowCellHints: Set<CellExtension>?
        val rowCells: Map<Key<T>, Cell<T>>?

        init {
            rowHints = collectHints(matchingRows) { r -> r.rowExtensions }
            rowCellHints = collectHints(matchingRows) { r -> r.cellExtensions }
            rowCells = collectCells(matchingRows)
        }

        private fun <E : Extension> collectHints(
            matchingRows: Set<Row<T>>?,
            getHints: (r: Row<T>) -> Set<E>?
        ): Set<E>? {
            return matchingRows?.mapNotNull { e -> getHints.invoke(e) }
                ?.fold(setOf(), { acc, r -> acc + r })
        }

        private fun collectCells(matchingRows: Set<Row<T>>?): Map<Key<T>, Cell<T>>? {
            return matchingRows?.mapNotNull { row -> row.cells }
                ?.fold(mapOf(), { acc, m -> acc + m })
        }
    }

    fun create(): DelegateAPI<A> {
        return delegate.createDocumentOperation.createDocument()
    }

    fun add(state: DelegateAPI<A>, table: Table<T>, collection: Collection<T>): DelegateAPI<A> {
        return ExportingState(
            delegate = delegate.createTableOperation.createTable(state, table),
            tableName = table.name ?: "table-${NextId.nextId()}",
            firstRow = table.firstRow,
            firstColumn = table.firstColumn
        ).also {
            if (table.showHeader == true || table.columns.any { column -> column.columnTitle != null }) {
                renderHeaderRow(it, table, collection)
            }
        }.also {
            renderSubsequentSyntheticRows(it, table, collection)
            renderCollectionRows(it, table, collection)
        }.delegate
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

    private fun renderCollectionRows(exportingState: ExportingState<A>, table: Table<T>, collection: Collection<T>) {
        collection.forEachIndexed { objectIndex: Int, record: T ->
            renderRow(
                exportingState,
                table,
                exportingState.rowContext(dataset = collection, objectIndex = objectIndex, record = record)
            ).also {
                renderSubsequentSyntheticRows(exportingState.nextRowIndex(), table, collection)
            }
        }
    }

    private fun renderSubsequentSyntheticRows(
        exportingState: ExportingState<A>,
        table: Table<T>,
        collection: Collection<T>
    ) {
        subsequentSyntheticRowsStartingAtRowIndex(exportingState.rowIndex, table.rows).let {
            it?.forEach { _ ->
                renderRow(exportingState, table, exportingState.rowContext(dataset = collection))
                exportingState.nextRowIndex()
            }
        }
    }

    private fun renderHeaderRow(
        state: ExportingState<A>,
        table: Table<T>,
        collection: Collection<T>
    ): ExportingState<A> {
        val headerRowMeta =
            findRowMatchingRules(RowData(rowIndex = state.rowIndex, dataset = collection), table.rows)
        return delegate.rowOperation.renderRow(state.delegate, state.coordinates(), headerRowMeta.rowHints).let {
            table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                delegate.columnOperation?.renderColumn(
                    state.delegate,
                    state.withColumnIndex(column.index ?: columnIndex).coordinates(),
                    column.columnExtensions
                )
                val cellDef = headerRowMeta.rowCells?.get(column.id)
                renderHeaderCell(
                    state,
                    column.columnTitle,
                    collectUniqueCellHints(
                        table.cellExtensions,
                        column.cellExtensions,
                        headerRowMeta.rowCellHints,
                        cellDef?.cellExtensions
                    )
                )
            }
        }.let { state.nextRowIndex() }
    }

    private fun renderHeaderCell(
        state: ExportingState<A>,
        columnTitle: Description?,
        cellHints: Set<CellExtension>?
    ): ExportingState<A> {
        return columnTitle?.let {
            delegate.headerCellOperation?.renderHeaderCell(
                state.delegate,
                state.coordinates(),
                columnTitle,
                cellHints
            ); state
        } ?: state
    }

    private fun renderRow(state: ExportingState<A>, rowHints: Set<RowExtension>?): ExportingState<A> {
        return delegate.rowOperation.renderRow(state.delegate, state.coordinates(), rowHints).let { state }
    }

    private fun renderRowCell(
        state: ExportingState<A>,
        value: CellValue?,
        cellHints: Set<CellExtension>?
    ): ExportingState<A> {
        return delegate.rowCellOperation?.renderRowCell(state.delegate, state.coordinates(), value, cellHints)
            .let { state }
    }

    private fun evalColumnExpr(column: Column<T>, row: RowData<T>): Any? {
        return row.record?.let {
            column.id.ref?.invoke(it)
        }
    }

    private fun renderRow(
        state: ExportingState<A>,
        table: Table<T>,
        row: RowData<T>
    ) {
        val rowWideRules = findRowMatchingRules(row, table.rows)
        renderRow(state, rowWideRules.rowHints).also {
            table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                val cellDef = rowWideRules.rowCells?.get(column.id)
                val value = (cellDef?.eval?.invoke(row) ?: cellDef?.value ?: evalColumnExpr(column, row))?.let {
                    column.dataFormatter?.invoke(it) ?: it
                }
                renderRowCell(
                    it.withColumnIndex(column.index ?: columnIndex),
                    value?.let { CellValue(value, cellDef?.type ?: column.columnType) },
                    collectUniqueCellHints(
                        table.cellExtensions,
                        column.cellExtensions,
                        rowWideRules.rowCellHints,
                        cellDef?.cellExtensions
                    )
                )
            }
        }
    }

    private fun allSelectableRows(tableRows: List<Row<T>>?): List<Row<T>>? = tableRows?.filter { it.selector != null }

    private fun allSyntheticRows(tableRows: List<Row<T>>?): List<Row<T>>? =
        tableRows?.filter { it.createAt != null }?.sortedBy { it.createAt }

    private fun subsequentSyntheticRowsStartingAtRowIndex(startFrom: Int = 0, tableRows: List<Row<T>>?): List<Row<T>>? {
        return AtomicInteger(startFrom).let {
            allSyntheticRows(tableRows)?.filter { row -> row.createAt!! >= it.get() }
                ?.takeWhile { row -> row.createAt == it.getAndIncrement() }
        }
    }

    private fun trailingSyntheticRows(tableRows: List<Row<T>>?, startingFrom: Int) {
        allSyntheticRows(tableRows)?.filter { it.createAt!! >= startingFrom }
    }

    private fun findRowMatchingRules(row: RowData<T>, tableRows: List<Row<T>>?): MergedRow<T> {
        val matchingSelectableRows = allSelectableRows(tableRows)?.filter { it.selector!!.invoke(row) }?.toSet()
        val createAtRow = allSyntheticRows(tableRows)?.filter { it.createAt == row.rowIndex }?.toSet()
        return MergedRow(createAtRow?.let { matchingSelectableRows?.plus(it) ?: it } ?: matchingSelectableRows)
    }

    private fun collectUniqueCellHints(vararg hintsOnLevels: Set<CellExtension>?): Set<CellExtension> {
        return hintsOnLevels.filterNotNull().fold(setOf(), { acc, s -> acc + s })
    }
}

fun <T, A> Collection<T>.exportTo(table: Table<T>, delegate: ExportOperations<T, A>, stream: OutputStream) {
    DataExportTemplate(delegate).export(table, this, stream)
}

