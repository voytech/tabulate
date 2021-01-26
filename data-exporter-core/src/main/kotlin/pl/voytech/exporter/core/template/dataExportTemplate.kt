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
open class DataExportTemplate<T, A>(private val delegate: ExportOperations<T, A>) {

    private fun create(): A {
        return delegate.lifecycleOperations.createDocument()
    }

    private fun add(api: A, table: Table<T>, collection: Collection<T>): A {
        GlobalContextAndAttributes(
            tableModel = delegate.tableOperations.createTable(api, table),
            tableName = table.name ?: "table-${NextId.nextId()}",
            firstRow = table.firstRow,
            firstColumn = table.firstColumn
        ).also {
            renderColumns(it, api, ColumnRenderPhase.BEFORE_FIRST_ROW)
            with(OperationContextIterator(RowContextResolver(table, it, collection))) {
                while (this.hasNext()) {
                    renderNextRow(it, api, this)
                }
            }
            renderColumns(it, api, ColumnRenderPhase.AFTER_LAST_ROW)
        }
        return api
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

    private fun renderColumns(
        stateAndAttributes: GlobalContextAndAttributes<T>,
        api: A,
        renderPhase: ColumnRenderPhase
    ) {
        stateAndAttributes.tableModel.forEachColumn { columnIndex: Int, column: Column<T> ->
            delegate.tableOperations.renderColumn(
                api, stateAndAttributes.getColumnContext(IndexedValue(column.index ?: columnIndex, column), renderPhase)
            )
        }
    }

    private fun renderRowCells(stateAndAttributes: GlobalContextAndAttributes<T>, api: A, context: RowOperationContext<T>) {
        stateAndAttributes.tableModel.forEachColumn { columnIndex: Int, column: Column<T> ->
            if (context.data?.rowCellValues?.containsKey(column.id) == true) {
                delegate.tableOperations.renderRowCell(
                    api,
                    stateAndAttributes.getCellContext(IndexedValue(column.index ?: columnIndex, column))
                )
            }
        }
    }

    private fun renderNextRow(stateAndAttributes: GlobalContextAndAttributes<T>, api: A, iterator: OperationContextIterator<T, RowOperationContext<T>>) {
        if (iterator.hasNext()) {
            iterator.next().let { rowContext ->
                delegate.tableOperations.renderRow(api, rowContext).also {
                    renderRowCells(stateAndAttributes, api, rowContext)
                }
            }
        }
    }
}

fun <T, A> Collection<T>.exportTable(table: Table<T>, delegate: ExportOperations<T, A>, stream: OutputStream) {
    DataExportTemplate(delegate).export(table, this, stream)
}

fun <T, A> Table<T>.exportWith(delegate: ExportOperations<T, A>, stream: OutputStream) {
    DataExportTemplate(delegate).export(this, stream)
}
