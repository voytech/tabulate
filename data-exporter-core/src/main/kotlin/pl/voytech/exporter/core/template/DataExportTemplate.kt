package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.Extension
import pl.voytech.exporter.core.model.extension.RowExtension
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger

/**
 * Core logic responsible for orchestrating rendering of tabular data format file.
 * Takes delegate object with bunch of specialised interfaces. Each interface defines contract for
 * single atomic step of data export.
 * Classes implementing interfaces must agree (via generics) on delegate state or low level API class in order to make
 * low level 3rd party API instance object (like POI workbooks) shared amongst all 'render step' interfaces.
 * When delegate state types matches at compile time, DataExportTemplate will pass initialized API object/ state amongst those
 * interface implementations.
 * @author Wojciech Mąka
 */
open class DataExportTemplate<T, A>(private val delegate: ExportOperations<T, A>) {

    private var selectableRows: List<Row<T>>? = null
    private var selectableRowsCached = false
    private var syntheticRows: List<Row<T>>? = null
    private var syntheticRowsCached = false

    internal data class ComputedSyntheticRowValue<T>(val matchingRowDefinitions: Set<Row<T>>?) {
        val rowExtensions: Set<RowExtension>?
        val rowCellExtensions: Set<CellExtension>?
        val rowCells: Map<Key<T>, Cell<T>>?

        init {
            rowExtensions = collectHints(matchingRowDefinitions) { r -> r.rowExtensions }
            rowCellExtensions = collectHints(matchingRowDefinitions) { r -> r.cellExtensions }
            rowCells = collectCells(matchingRowDefinitions)
        }

        private inline fun <E : Extension> collectHints(
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

    data class ComputedRowValue<T>(
        val rowExtensions: Set<RowExtension>?,
        val rowCellExtensions: Set<CellExtension>?,
        val rowCells: Map<Key<T>, Cell<T>>?,
        val rowCellValues: Map<Key<T>, CellValue?>,
        val typedRow: TypedRowData<T>
    )

    fun create(): DelegateAPI<A> {
        return delegate.lifecycleOperations.createDocument()
    }

    fun add(state: DelegateAPI<A>, table: Table<T>, collection: Collection<T>): DelegateAPI<A> {
        return ExportingState(
            delegate = delegate.tableOperations.createTable(state, table),
            tableName = table.name ?: "table-${NextId.nextId()}",
            firstRow = table.firstRow,
            firstColumn = table.firstColumn,
            collection = collection
        ).also {
            preFlightPass(it, table, collection)
        }.also {
            renderColumns(it, table, ColumnRenderPhase.BEFORE_FIRST_ROW)
        }.also {
            renderRows(it, table)
        }.also {
            renderColumns(it, table, ColumnRenderPhase.AFTER_LAST_ROW)
        }.also {
            syntheticRows = null
            selectableRows = null
            syntheticRowsCached = false
            selectableRowsCached = false
        }.delegate
    }

    fun export(table: Table<T>, collection: Collection<T>): FileData<ByteArray> {
        return add(create(), table, collection).let { delegate.lifecycleOperations.saveDocument(it) }
    }

    fun export(table: Table<T>, collection: Collection<T>, stream: OutputStream) {
        add(create(), table, collection).also { delegate.lifecycleOperations.saveDocument(it, stream) }
    }

    fun export(state: DelegateAPI<A>, stream: OutputStream) {
        delegate.lifecycleOperations.saveDocument(state, stream)
    }

    /**
     * Build multidimensional cache of table structure, where each row has associated extensions as long as effective cell values
     * resolved from synthetic (definition-time) values or source dataset values. After preflight is done we have context data
     * for each scoped operation (for row, column, cell operations)
     */
    private fun preFlightPass(exportingState: ExportingState<T, A>, table: Table<T>, collection: Collection<T>) {
        val rowIndex = AtomicInteger(0)
        val preFlightSyntheticRows = {
            subsequentSyntheticRowsStartingAtRowIndex(rowIndex.get(), table.rows).let {
                it?.forEach { _ ->
                    computeRowValue(
                        exportingState,
                        table,
                        TypedRowData(rowIndex = rowIndex.getAndIncrement(), dataset = collection)
                    )
                }
            }
            exportingState
        }
        preFlightSyntheticRows().also {
            collection.forEachIndexed { objectIndex: Int, record: T ->
                computeRowValue(
                    exportingState,
                    table,
                    TypedRowData(
                        dataset = collection,
                        rowIndex = rowIndex.getAndIncrement(),
                        objectIndex = objectIndex,
                        record = record
                    )
                ).also { preFlightSyntheticRows() }
            }
        }
    }

    private fun computeRowValue(
        exportingState: ExportingState<T, A>,
        table: Table<T>,
        typedRow: TypedRowData<T>
    ): ExportingState<T, A> {
        val syntheticRowValue = computeSyntheticRowValue(typedRow, table.rows)
        val cellValues: MutableMap<Key<T>, CellValue?> = mutableMapOf()
        table.columns.forEachIndexed { _: Int, column: Column<T> ->
            val syntheticCell = syntheticRowValue.rowCells?.get(column.id)
            val effectiveRawCellValue =
                (syntheticCell?.eval?.invoke(typedRow) ?: syntheticCell?.value ?: evalColumnExpr(
                    column,
                    typedRow
                ))?.let {
                    column.dataFormatter?.invoke(it) ?: it
                }
            cellValues[column.id] = effectiveRawCellValue?.let {
                CellValue(
                    effectiveRawCellValue,
                    syntheticCell?.type ?: column.columnType
                )
            }
        }
        return exportingState.addRow(
            ComputedRowValue(
                rowExtensions = syntheticRowValue.rowExtensions,
                rowCellExtensions = syntheticRowValue.rowCellExtensions,
                rowCells = syntheticRowValue.rowCells,
                rowCellValues = cellValues.toMap(),
                typedRow = typedRow
            )
        )
    }

    private fun renderColumns(
        state: ExportingState<T, A>,
        table: Table<T>,
        renderPhase: ColumnRenderPhase
    ) {
        table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
            delegate.tableOperations.let {
                it.renderColumn(
                    state.delegate,
                    state.columnOperationContext(column.index ?: columnIndex, column.id, renderPhase),
                    column.columnExtensions?.filter { ext ->
                        ((ColumnRenderPhase.BEFORE_FIRST_ROW == renderPhase) && ext.beforeFirstRow()) ||
                                ((ColumnRenderPhase.AFTER_LAST_ROW == renderPhase) && ext.afterLastRow())
                    }?.toSet()
                )
            }
        }
    }

    private fun renderRows(
        state: ExportingState<T, A>,
        table: Table<T>
    ) {
        state.forEachRowValue { rowValue: ComputedRowValue<T> ->
            delegate.tableOperations.renderRow(state.delegate, state.rowOperationContext(), rowValue.rowExtensions)
                .also {
                    table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                        delegate.tableOperations.renderRowCell(
                            state.delegate,
                            state.cellOperationContext(column.index ?: columnIndex, column.id),
                            mergeCellHints(
                                rowValue.rowCells?.get(column.id)?.cellExtensions,
                                rowValue.rowCellExtensions,
                                column.cellExtensions,
                                table.cellExtensions
                            )
                        )
                    }
                }
        }
    }

    private fun evalColumnExpr(column: Column<T>, typedRow: TypedRowData<T>): Any? {
        return typedRow.record?.let {
            column.id.ref?.invoke(it)
        }
    }

    private fun allSelectableRows(tableRows: List<Row<T>>?): List<Row<T>>? {
        if (selectableRows == null && !selectableRowsCached) {
            selectableRows = tableRows?.filter { it.selector != null }
            selectableRowsCached = true
        }
        return selectableRows
    }

    private fun allSyntheticRows(tableRows: List<Row<T>>?): List<Row<T>>? {
        if (syntheticRows == null && !syntheticRowsCached) {
            syntheticRows = tableRows?.filter { it.createAt != null }?.sortedBy { it.createAt }
            syntheticRowsCached = true
        }
        return syntheticRows
    }

    private fun subsequentSyntheticRowsStartingAtRowIndex(startFrom: Int = 0, tableRows: List<Row<T>>?): List<Row<T>>? {
        return AtomicInteger(startFrom).let {
            allSyntheticRows(tableRows)?.filter { row -> row.createAt!! >= it.get() }
                ?.takeWhile { row -> row.createAt == it.getAndIncrement() }
        }
    }

    private fun computeSyntheticRowValue(
        typedRow: TypedRowData<T>,
        tableRows: List<Row<T>>?
    ): ComputedSyntheticRowValue<T> {
        val matchingSelectableRows = allSelectableRows(tableRows)?.filter { it.selector!!.invoke(typedRow) }?.toSet()
        val createAtRow = allSyntheticRows(tableRows)?.filter { it.createAt == typedRow.rowIndex }?.toSet()
        return ComputedSyntheticRowValue(createAtRow?.let { matchingSelectableRows?.plus(it) ?: it }
            ?: matchingSelectableRows)
    }

    private fun mergeCellHints(vararg hintsOnLevels: Set<CellExtension>?): Set<CellExtension> {
        return hintsOnLevels.filterNotNull().fold(setOf(), { acc, s -> acc + s })
    }
}

fun <T, A> Collection<T>.exportTo(table: Table<T>, delegate: ExportOperations<T, A>, stream: OutputStream) {
    DataExportTemplate(delegate).export(table, this, stream)
}