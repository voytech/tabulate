package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.api.builder.dsl.TableBuilderApi
import pl.voytech.exporter.core.api.builder.dsl.table
import pl.voytech.exporter.core.model.Column
import pl.voytech.exporter.core.model.NextId
import pl.voytech.exporter.core.template.TabulateOutputStream.Companion.into
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.ColumnRenderPhase
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes
import pl.voytech.exporter.core.template.iterators.OperationContextIterator
import pl.voytech.exporter.core.template.operations.ExportOperations
import pl.voytech.exporter.core.template.resolvers.RowContextResolver
import pl.voytech.exporter.core.template.spi.ExportOperationFactoryProvider
import pl.voytech.exporter.core.template.spi.Identifiable
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

    constructor(output: TabulateOutputStream) : this() {
        this.delegate = locate(output)!!.create<T>().createOperations();
    }

    constructor(delegate: ExportOperations<T>) : this() {
        this.delegate = delegate
    }

    private fun initialize() {
        delegate.lifecycleOperations.initialize()
    }

    private fun bind(tableBuilder: TableBuilder<T>, collection: Collection<T>) {
        delegate.lifecycleOperations.createTable(tableBuilder).let { table ->
            GlobalContextAndAttributes(
                tableModel = table,
                tableName = table.name ?: "table-${NextId.nextId()}",
                firstRow = table.firstRow,
                firstColumn = table.firstColumn
            ).also {
                renderColumns(it, ColumnRenderPhase.BEFORE_FIRST_ROW)
                with(OperationContextIterator(RowContextResolver(table, it, collection))) {
                    while (hasNext()) {
                        renderNextRow(it, this)
                    }
                }
                renderColumns(it, ColumnRenderPhase.AFTER_LAST_ROW)
            }
        }
    }

    private fun locate(id: Identifiable): ExportOperationFactoryProvider? {
        val loader: ServiceLoader<ExportOperationFactoryProvider> =
            ServiceLoader.load(ExportOperationFactoryProvider::class.java)
        return loader.find { it.test(id) }
    }

    fun export(tableBuilder: TableBuilder<T>, collection: Collection<T>, stream: OutputStream) {
        initialize()
        bind(tableBuilder, collection).also { delegate.lifecycleOperations.finish(stream) }
    }

    fun export(tableBuilder: TableBuilder<T>, stream: OutputStream) {
        initialize()
        bind(tableBuilder, emptyList()).also { delegate.lifecycleOperations.finish(stream) }
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

fun <T> Collection<T>.tabulate(tableBuilder: TableBuilder<T>, operations: ExportOperations<T>, stream: OutputStream) {
    TableExportTemplate(operations).export(tableBuilder, this, stream)
}

fun <T> Collection<T>.tabulate(output: TabulateOutputStream, block: TableBuilderApi<T>.() -> Unit) {
    output.use {
        TableExportTemplate<T>(output).export(table(block), this, it)
    }
}

fun <T> Collection<T>.tabulate(id: String, stream: OutputStream, block: TableBuilderApi<T>.() -> Unit) {
    this.tabulate(into(id,stream), block)
}

fun <T> Collection<T>.tabulate(file: File, block: TableBuilderApi<T>.() -> Unit) {
    this.tabulate(into(file), block)
}

fun <T> Collection<T>.tabulate(fileName: String, block: TableBuilderApi<T>.() -> Unit) {
    this.tabulate(into(fileName), block)
}

fun <T> TableBuilder<T>.export(operations: ExportOperations<T>, stream: OutputStream) {
    TableExportTemplate(operations).export(this, stream)
}

fun <T> TableBuilder<T>.export(file: File) {
    into(file).use {
        TableExportTemplate<T>(it).export(this,  it)
    }
}
