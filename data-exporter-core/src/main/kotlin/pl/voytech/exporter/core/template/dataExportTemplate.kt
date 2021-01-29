package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.iterators.OperationContextIterator
import pl.voytech.exporter.core.template.operations.ExportOperations
import pl.voytech.exporter.core.template.resolvers.RowContextResolver
import java.io.OutputStream

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
open class DataExportTemplate<T>(private val delegate: ExportOperations<T>) {

    private fun initialize() {
        delegate.lifecycleOperations.initialize()
    }

    private fun add(table: Table<T>, collection: Collection<T>) {
        GlobalContextAndAttributes(
            tableModel = delegate.tableOperations.createTable(table),
            tableName = table.name ?: "table-${NextId.nextId()}",
            firstRow = table.firstRow,
            firstColumn = table.firstColumn
        ).also {
            renderColumns(it, ColumnRenderPhase.BEFORE_FIRST_ROW)
            with(OperationContextIterator(RowContextResolver(table, it, collection))) {
                while (this.hasNext()) {
                    renderNextRow(it, this)
                }
            }
            renderColumns(it, ColumnRenderPhase.AFTER_LAST_ROW)
        }
    }

    fun export(table: Table<T>, collection: Collection<T>, stream: OutputStream) {
        initialize()
        add(table, collection).also { delegate.lifecycleOperations.finish(stream) }
    }

    fun export(table: Table<T>, stream: OutputStream) {
        initialize()
        add(table, emptyList()).also { delegate.lifecycleOperations.finish(stream) }
    }

    fun export(stream: OutputStream) {
        delegate.lifecycleOperations.finish(stream)
    }

    private fun renderColumns(stateAndAttributes: GlobalContextAndAttributes<T>, renderPhase: ColumnRenderPhase) {
        stateAndAttributes.tableModel.forEachColumn { columnIndex: Int, column: Column<T> ->
            delegate.tableOperations.renderColumn(
                stateAndAttributes.createColumnContext(IndexedValue(column.index ?: columnIndex, column), renderPhase)
            )
        }
    }

    private fun renderRowCells(stateAndAttributes: GlobalContextAndAttributes<T>, context: AttributedRow<T>) {
        stateAndAttributes.tableModel.forEachColumn { column: Column<T> ->
            if (context.rowCellValues.containsKey(column.id)) {
                delegate.tableOperations.renderRowCell(context.rowCellValues[column.id]!!)
            }
        }
    }

    private fun renderNextRow(
        stateAndAttributes: GlobalContextAndAttributes<T>,
        iterator: OperationContextIterator<T, AttributedRow<T>>
    ) {
        if (iterator.hasNext()) {
            iterator.next().let { rowContext ->
                delegate.tableOperations.renderRow(rowContext).also {
                    renderRowCells(stateAndAttributes, rowContext)
                }
            }
        }
    }
}

fun <T> Collection<T>.exportTable(table: Table<T>, delegate: ExportOperations<T>, stream: OutputStream) {
    DataExportTemplate(delegate).export(table, this, stream)
}

fun <T> Table<T>.exportWith(delegate: ExportOperations<T>, stream: OutputStream) {
    DataExportTemplate(delegate).export(this, stream)
}
