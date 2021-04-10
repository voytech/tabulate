package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.Column
import pl.voytech.exporter.core.model.NextId
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.ColumnRenderPhase
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes
import pl.voytech.exporter.core.template.iterators.OperationContextIterator
import pl.voytech.exporter.core.template.operations.ExportOperations
import pl.voytech.exporter.core.template.resolvers.RowContextResolver
import pl.voytech.exporter.core.template.spi.ExportOperationFactoryProvider
import java.io.File
import java.io.OutputStream
import java.util.*


/**
 * Core instrumentation logic enters from this place. This class constitutes all entry points and public API for tabular
 * data exporting.
 * @author Wojciech Mąka
 */
open class TableExportTemplate<T>() {

    private lateinit var delegate: ExportOperations<T>

    constructor(id: String) : this() {
        this.delegate = locate(id)!!.create<T>().createOperations();
    }

    constructor(delegate: ExportOperations<T>) : this() {
        this.delegate = delegate
    }

    private fun initialize() {
        delegate.lifecycleOperations.initialize()
    }

    private fun add(tableBuilder: TableBuilder<T>, collection: Collection<T>) {
        delegate.tableRenderOperations.createTable(tableBuilder).let { table ->
            GlobalContextAndAttributes(
                tableModel = table,
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
    }

    private fun locate(id: String) : ExportOperationFactoryProvider? {
        val loader: ServiceLoader<ExportOperationFactoryProvider> = ServiceLoader.load(ExportOperationFactoryProvider::class.java)
        return loader.find { it.id() == id }
    }

    fun export(tableBuilder: TableBuilder<T>, collection: Collection<T>, stream: OutputStream) {
        initialize()
        add(tableBuilder, collection).also { delegate.lifecycleOperations.finish(stream) }
    }

    fun export(tableBuilder: TableBuilder<T>, stream: OutputStream) {
        initialize()
        add(tableBuilder, emptyList()).also { delegate.lifecycleOperations.finish(stream) }
    }

    fun export(stream: OutputStream) {
        delegate.lifecycleOperations.finish(stream)
    }

    private fun renderColumns(stateAndAttributes: GlobalContextAndAttributes<T>, renderPhase: ColumnRenderPhase) {
        stateAndAttributes.tableModel.forEachColumn { columnIndex: Int, column: Column<T> ->
            delegate.tableRenderOperations.renderColumn(
                stateAndAttributes.createColumnContext(IndexedValue(column.index ?: columnIndex, column), renderPhase)
            )
        }
    }

    private fun renderRowCells(stateAndAttributes: GlobalContextAndAttributes<T>, context: AttributedRow<T>) {
        stateAndAttributes.tableModel.forEachColumn { column: Column<T> ->
            if (context.rowCellValues.containsKey(column.id)) {
                delegate.tableRenderOperations.renderRowCell(context.rowCellValues[column.id]!!)
            }
        }
    }

    private fun renderNextRow(
        stateAndAttributes: GlobalContextAndAttributes<T>,
        iterator: OperationContextIterator<T, AttributedRow<T>>
    ) {
        if (iterator.hasNext()) {
            iterator.next().let { rowContext ->
                delegate.tableRenderOperations.renderRow(rowContext).also {
                    renderRowCells(stateAndAttributes, rowContext)
                }
            }
        }
    }
}

fun <T> Collection<T>.tabulate(tableBuilder: TableBuilder<T>, delegate: ExportOperations<T>, stream: OutputStream) {
    TableExportTemplate(delegate).export(tableBuilder, this, stream)
}

fun <T> Collection<T>.tabulate(tableBuilder: TableBuilder<T>, id: String, stream: OutputStream) {
    TableExportTemplate<T>(id).export(tableBuilder, this, stream)
}

fun <T> Collection<T>.tabulate(tableBuilder: TableBuilder<T>, file : File) {
    file.outputStream().use {
        TableExportTemplate<T>(file.extension).export(tableBuilder, this, it)
    }
}

fun <T> TableBuilder<T>.export(delegate: ExportOperations<T>, stream: OutputStream) {
    TableExportTemplate(delegate).export(this, stream)
}
