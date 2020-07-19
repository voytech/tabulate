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
 * When the type is same on compile time, DataExportTemplate will pass initialized API object/ state amongst those
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

    data class ComputedRowValue<T>(
        val rowExtensions: Set<RowExtension>?,
        val rowCellExtensions: Set<CellExtension>?,
        val rowCells: Map<Key<T>, Cell<T>>?,
        val rowCellValues: Map<Key<T>, CellValue?>,
        val typedRow: TypedRowData<T>
    )

    fun create(): DelegateAPI<A> {
        return delegate.createDocumentOperation.createDocument()
    }

    fun add(state: DelegateAPI<A>, table: Table<T>, collection: Collection<T>): DelegateAPI<A> {
        return ExportingState(
            delegate = delegate.createTableOperation.createTable(state, table),
            tableName = table.name ?: "table-${NextId.nextId()}",
            firstRow = table.firstRow,
            firstColumn = table.firstColumn,
            collection = collection
        ).also { preFlightPass(it, table, collection)
        }.also { renderColumns(it, table)
        }.also { renderRows(it, table)
        }.also {
            syntheticRows = null
            selectableRows = null
            syntheticRowsCached = false
            selectableRowsCached = false
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
                (syntheticCell?.eval?.invoke(typedRow) ?: syntheticCell?.value ?: evalColumnExpr(column, typedRow))?.let {
                    column.dataFormatter?.invoke(it) ?: it
                }
            cellValues[column.id] = effectiveRawCellValue?.let {
                CellValue(
                    effectiveRawCellValue,
                    syntheticCell?.type ?: column.columnType
                )
            }
        }
        return exportingState.addRow(ComputedRowValue(
            rowExtensions = syntheticRowValue.rowExtensions,
            rowCellExtensions = syntheticRowValue.rowCellExtensions,
            rowCells = syntheticRowValue.rowCells,
            rowCellValues = cellValues.toMap(),
            typedRow = typedRow
        ))
    }

    private fun renderColumns(
        state: ExportingState<T, A>,
        table: Table<T>
    ) {
        table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
            delegate.columnOperation?.renderColumn(
                state.delegate,
                state.columnOperationContext(column.index ?: columnIndex, column.id),
                column.columnExtensions
            )
        }
    }

    private fun renderRows(
        state: ExportingState<T, A>,
        table: Table<T>
    ) {
        state.forEachRowValue { rowValue: ComputedRowValue<T> ->
            delegate.rowOperation.renderRow(state.delegate, state.rowOperationContext(), rowValue.rowExtensions).also {
                table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                    delegate.rowCellOperation?.renderRowCell(
                        state.delegate,
                        state.cellOperationContext(column.index ?: columnIndex, column.id),
                        collectUniqueCellHints(
                            table.cellExtensions,
                            column.cellExtensions,
                            rowValue.rowCellExtensions,
                            rowValue.rowCells?.get(column.id)?.cellExtensions
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

    private fun computeSyntheticRowValue(typedRow: TypedRowData<T>, tableRows: List<Row<T>>?): ComputedSyntheticRowValue<T> {
        val matchingSelectableRows = allSelectableRows(tableRows)?.filter { it.selector!!.invoke(typedRow) }?.toSet()
        val createAtRow = allSyntheticRows(tableRows)?.filter { it.createAt == typedRow.rowIndex }?.toSet()
        return ComputedSyntheticRowValue(createAtRow?.let { matchingSelectableRows?.plus(it) ?: it }
            ?: matchingSelectableRows)
    }

    private fun collectUniqueCellHints(vararg hintsOnLevels: Set<CellExtension>?): Set<CellExtension> {
        return hintsOnLevels.filterNotNull().fold(setOf(), { acc, s -> acc + s })
    }
}

fun <T, A> Collection<T>.exportTo(table: Table<T>, delegate: ExportOperations<T, A>, stream: OutputStream) {
    DataExportTemplate(delegate).export(table, this, stream)
}




/*
TODO REMOVE!
private fun renderHeaderRow(
    state: ExportingState<T, A>,
    table: Table<T>,
    collection: Collection<T>
): ExportingState<T, A> {
    val headerRowMeta =
        computeSyntheticRowValue(TypedRowData(rowIndex = state.rowIndex, dataset = collection), table.rows)
    return delegate.rowOperation.renderRow(state.delegate, state.rowOperationContext(), headerRowMeta.rowExtensions)
        .let {
            table.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                delegate.columnOperation?.renderColumn(
                    state.delegate,
                    state.withColumnIndex(column.index ?: columnIndex).columnOperationContext(),
                    column.columnExtensions
                )
                val cellDef = headerRowMeta.rowCells?.get(column.id)
                renderHeaderCell(
                    state,
                    column.columnTitle,
                    collectUniqueCellHints(
                        table.cellExtensions,
                        column.cellExtensions,
                        headerRowMeta.rowCellExtensions,
                        cellDef?.cellExtensions
                    )
                )
            }
        }.let { state.nextRowIndex() }
}

private fun renderHeaderCell(
    state: ExportingState<T, A>,
    columnTitle: Description?,
    cellHints: Set<CellExtension>?
): ExportingState<T, A> {
    return columnTitle?.let {
        delegate.headerCellOperation?.renderHeaderCell(
            state.delegate,
            state.cellOperationContext(),
            columnTitle,
            cellHints
        ); state
    } ?: state
} */