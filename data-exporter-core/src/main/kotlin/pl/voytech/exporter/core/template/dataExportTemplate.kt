package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.CellAttribute
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
    private var customRows: List<Row<T>>? = null
    private var customRowsCached = false

    private fun create(): A {
        return delegate.lifecycleOperations.createDocument()
    }

    private fun add(state: A, table: Table<T>, collection: Collection<T>): A {
        return ExporterSession(
            delegate = state,
            tableModel = delegate.tableOperations.createTable(state, table),
            tableName = table.name ?: "table-${NextId.nextId()}",
            firstRow = table.firstRow,
            firstColumn = table.firstColumn,
            collection = collection
        ).also {
            preFlightPass(it, collection)
        }.also {
            renderColumns(it, ColumnRenderPhase.BEFORE_FIRST_ROW)
        }.also {
            renderRows(it)
        }.also {
            renderColumns(it, ColumnRenderPhase.AFTER_LAST_ROW)
        }.also {
            customRows = null
            selectableRows = null
            customRowsCached = false
            selectableRowsCached = false
        }.delegate
    }

    fun export(table: Table<T>, collection: Collection<T>): FileData<ByteArray> {
        return add(create(), table, collection).let { delegate.lifecycleOperations.saveDocument(it) }
    }

    fun export(table: Table<T>, collection: Collection<T>, stream: OutputStream) {
        add(create(), table, collection).also { delegate.lifecycleOperations.saveDocument(it, stream) }
    }

    fun export(table: Table<T>, stream: OutputStream) {
        add(create(), table, emptyList()).also { delegate.lifecycleOperations.saveDocument(it, stream) }
    }

    fun export(state: A, stream: OutputStream) {
        delegate.lifecycleOperations.saveDocument(state, stream)
    }

    /**
     * Build multidimensional table structure, where each row has associated extensions as long as effective cell values
     * resolved from synthetic (definitions from builders) values or source dataset values. After pre-flight is everything to build a context data
     * for scoped operation (for row, column, cell) when exporter is progressing over collection data.
     */
    private fun preFlightPass(exporterSession: ExporterSession<T, A>, collection: Collection<T>) {
        val rowIndex = AtomicInteger(0)
        val rowSkips = mutableMapOf<ColumnKey<T>, Int>()
        val preFlightCustomRows = {
            subsequentCustomRowsStartingAtRowIndex(rowIndex.get(), exporterSession.tableModel.rows).let {
                it?.forEach { _ ->
                    exporterSession.addRow(
                        computeRowValue(
                            exporterSession.tableModel,
                            TypedRowData(
                                rowIndex = rowIndex.getAndIncrement(),
                                dataset = collection
                            ),
                            rowSkips
                        )
                    )
                }
            }
            exporterSession
        }
        preFlightCustomRows().also {
            if (!collection.isEmpty()) {
                collection.forEachIndexed { objectIndex: Int, record: T ->
                    exporterSession.addRow(
                        computeRowValue(
                            it.tableModel,
                            TypedRowData(
                                dataset = collection,
                                rowIndex = rowIndex.getAndIncrement(),
                                objectIndex = objectIndex,
                                record = record
                            ),
                            rowSkips
                        )
                    ).also { preFlightCustomRows() }
                }
            }
        }
    }

    private fun renderColumns(
        state: ExporterSession<T, A>,
        renderPhase: ColumnRenderPhase
    ) {
        state.tableModel.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
            delegate.tableOperations.renderColumn(
                state.delegate,
                state.setColumnContext(column.index ?: columnIndex, column, renderPhase)
            )
        }
    }

    private fun renderRows(state: ExporterSession<T, A>) {
        state.forEachRowValue { context ->
            delegate.tableOperations.renderRow(state.delegate, context)
                .also {
                    state.tableModel.columns.forEachIndexed { columnIndex: Int, column: Column<T> ->
                        if (context.data?.rowCellValues?.containsKey(column.id) == true) {
                            delegate.tableOperations.renderRowCell(
                                state.delegate,
                                state.setCellContext(
                                    column.index ?: columnIndex,
                                    context.data.let { it?.rowCellValues?.get(column.id) ?: error("") }
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun allPredicateRows(tableRows: List<Row<T>>?): List<Row<T>>? {
        if (selectableRows == null && !selectableRowsCached) {
            selectableRows = tableRows?.filter { it.selector != null }
            selectableRowsCached = true
        }
        return selectableRows
    }

    private fun allCustomRows(tableRows: List<Row<T>>?): List<Row<T>>? {
        if (customRows == null && !customRowsCached) {
            customRows = tableRows?.filter { it.createAt != null }?.sortedBy { it.createAt }
            customRowsCached = true
        }
        return customRows
    }

    private fun subsequentCustomRowsStartingAtRowIndex(startFrom: Int = 0, tableRows: List<Row<T>>?): List<Row<T>>? {
        return AtomicInteger(startFrom).let {
            allCustomRows(tableRows)?.filter { row -> row.createAt!! >= it.get() }
                ?.takeWhile { row -> row.createAt == it.getAndIncrement() }
        }
    }

    private inline fun computeCellValue(
        column: Column<T>,
        customCell: Cell<T>?,
        rowData: TypedRowData<T>
    ): Any? {
        return (customCell?.eval?.invoke(rowData) ?: customCell?.value ?: rowData.record?.let {
            column.id.ref?.invoke(it)
        })?.let {
            column.dataFormatter?.invoke(it) ?: it
        }
    }

    private inline fun matchingPredicateRows(table: Table<T>, typedRow: TypedRowData<T>): Set<Row<T>>? =
        allPredicateRows(table.rows)?.filter { it.selector!!.invoke(typedRow) }?.toSet()

    private inline fun matchingCustomRows(table: Table<T>, typedRow: TypedRowData<T>): Set<Row<T>>? =
        allCustomRows(table.rows)?.filter { it.createAt == typedRow.rowIndex }?.toSet()

    private inline fun matchingRows(table: Table<T>, typedRow: TypedRowData<T>): Set<Row<T>>? {
        val matchingPredicateRows = matchingPredicateRows(table, typedRow)
        return matchingCustomRows(table, typedRow)?.let { matchingPredicateRows?.plus(it) ?: it }
            ?: matchingPredicateRows ?: emptySet()
    }

    private fun computeRowValue(
        table: Table<T>,
        typedRow: TypedRowData<T>,
        rowSkips: MutableMap<ColumnKey<T>, Int>
    ): AttributedRow<T> {
        val rowDefinitions: Set<Row<T>>? = matchingRows(table, typedRow)
        val mergedRowCells: Map<ColumnKey<T>, Cell<T>>? =
            rowDefinitions?.mapNotNull { row -> row.cells }?.fold(mapOf(), { acc, m -> acc + m })
        val cellValues: MutableMap<ColumnKey<T>, AttributedCell> = mutableMapOf()
        var columnSkips = 0
        val rowCellExtensions = mergeAttributes(
            *(rowDefinitions?.mapNotNull { row -> row.cellAttributes }!!.toTypedArray())
        )
        table.columns.forEach { column: Column<T> ->
            if (columnSkips-- <= 0 && (rowSkips[column.id] ?: 0).also { rowSkips[column.id] = it - 1 } <= 0) {
                val customCell = mergedRowCells?.get(column.id)
                columnSkips = (customCell?.colSpan?.minus(1)) ?: 0
                rowSkips[column.id] = (customCell?.rowSpan?.minus(1)) ?: 0
                val attributedCell = computeCellValue(column, customCell, typedRow)?.let {
                    AttributedCell(
                        value = CellValue(
                            it,
                            customCell?.type ?: column.columnType,
                            colSpan = customCell?.colSpan ?: 1,
                            rowSpan = customCell?.rowSpan ?: 1
                        ),
                        attributes = mergeAttributes(
                            table.cellAttributes,
                            column.cellAttributes,
                            rowCellExtensions,
                            customCell?.cellAttributes
                        )
                    )
                }
                if (attributedCell != null) {
                    cellValues[column.id] = attributedCell
                }
            }
        }
        return AttributedRow(
            rowAttributes = rowDefinitions.mapNotNull { it.rowAttributes }.fold(setOf(), { acc, r -> acc + r }),
            rowCellValues = cellValues.toMap()
        )
    }

    private fun mergeAttributes(vararg attributesByLevels: Set<CellAttribute>?): Set<CellAttribute>? {
        return attributesByLevels.filterNotNull()
            .map { set -> set.groupBy { it.javaClass }.map { Pair(it.key, it.value.first()) }.toMap() }
            .fold(
                mapOf<Class<CellAttribute>, CellAttribute>(),
                { accumulated, currentLevel -> mergeAttributes(accumulated, currentLevel) })
            .values
            .toSet()
    }

    private fun mergeAttributes(
        first: Map<Class<CellAttribute>, CellAttribute>,
        second: Map<Class<CellAttribute>, CellAttribute>
    ): Map<Class<CellAttribute>, CellAttribute> {
        val result = mutableMapOf<Class<CellAttribute>, CellAttribute>()
        first.keys.toSet().intersect(second.keys.toSet()).forEach {
            result[it] = (first[it] ?: error("")).mergeWith((second[it] ?: error("")))
        }
        first.keys.toSet().subtract(second.keys.toSet()).forEach { result[it] = first[it] ?: error("") }
        second.keys.toSet().subtract(first.keys.toSet()).forEach { result[it] = second[it] ?: error("") }
        return result.toMap()
    }
}

fun <T, A> Collection<T>.exportTable(table: Table<T>, delegate: ExportOperations<T, A>, stream: OutputStream) {
    DataExportTemplate(delegate).export(table, this, stream)
}

fun <T, A> Table<T>.exportWith(delegate: ExportOperations<T, A>, stream: OutputStream) {
    DataExportTemplate(delegate).export(this, stream)
}
