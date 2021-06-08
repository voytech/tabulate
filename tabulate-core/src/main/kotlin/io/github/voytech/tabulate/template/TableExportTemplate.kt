package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.model.ColumnDef
import io.github.voytech.tabulate.model.NextId
import io.github.voytech.tabulate.template.context.*
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.template.spi.Identifiable
import org.reactivestreams.Processor
import org.reactivestreams.Publisher
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

interface TableExportTemplateApi<T, O> {
    fun begin()
    fun nextRow(record: T)
    fun end(result: O)
}

/**
 * An entry point for exporting. Role of this class is to orchestrate and bind all things together.
 * @author Wojciech Mąka
 */
class TableExportTemplate<T, O, CTX : RenderingContext>(private val format: TabulationFormat) {

    private val provider: ExportOperationsProvider<T, O, CTX> by lazy {
        resolveExportOperationsFactory(format)
    }

    private val ops: TableExportOperations<T,O> by lazy {
        provider.create()
    }

    inner class TableExportTemplateApiImpl(private val state: TableExportingState<T>) : TableExportTemplateApi<T, O> {

        override fun begin() = renderColumns(state, ColumnRenderPhase.BEFORE_FIRST_ROW)

        override fun nextRow(record: T) = bufferRecordAndRenderRow(state, record)

        override fun end(result: O) {
            renderRemainingBufferedRows(state)
            renderRowsAfterDataProcessed(state)
            renderColumns(state, ColumnRenderPhase.AFTER_LAST_ROW)
            ops.finish(result)
        }
    }

    private fun createTable(tableBuilder: TableBuilder<T>): TableExportingState<T> {
        return ops.createTable(tableBuilder).let { table ->
            TableExportingState(
                tableModel = table,
                tableName = table.name ?: "table${NextId.nextId()}",
                firstRow = table.firstRow,
                firstColumn = table.firstColumn
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveExportOperationsFactory(id: Identifiable): ExportOperationsProvider<T, O, CTX> {
        val loader: ServiceLoader<ExportOperationsProvider<*, *, *>> =
            ServiceLoader.load(ExportOperationsProvider::class.java)
        return loader.find { it.test(id) } as ExportOperationsProvider<T, O, CTX>
    }

    fun <I> export(source: I, handler: TabulationHandler<I, T, O, CTX>, tableBuilder: TableBuilder<T>) {
        ops.initialize()
        createTable(tableBuilder).let {
            handler.orchestrate(source, TableExportTemplateApiImpl(it), provider.getRenderingContext())
        }
    }

    private fun bufferRecordAndRenderRow(state: TableExportingState<T>, record: T) {
        state.bufferAndNext(record)?.let { renderRow(state, it) }
    }

    private fun renderRemainingBufferedRows(state: TableExportingState<T>) {
        do {
            val context = state.getNextRowContext()?.let {
                renderRow(state, it)
                it
            }
        } while (context != null)
    }

    private fun renderRowsAfterDataProcessed(state: TableExportingState<T>) {
        state.mark(IndexLabel.DATASET_PROCESSED)
        renderRemainingBufferedRows(state)
    }

    private fun renderColumns(state: TableExportingState<T>, renderPhase: ColumnRenderPhase) {
        state.tableModel.forEachColumn { columnIndex: Int, column: ColumnDef<T> ->
            ops.renderColumn(
                state.createColumnContext(IndexedValue(column.index ?: columnIndex, column), renderPhase)
            )
        }
    }

    private fun renderRowCells(state: TableExportingState<T>, context: AttributedRow<T>) {
        state.tableModel.forEachColumn { column: ColumnDef<T> ->
            if (context.rowCellValues.containsKey(column.id)) {
                ops.renderRowCell(context.rowCellValues[column.id]!!)
            }
        }
    }

    private fun renderRow(state: TableExportingState<T>, rowContext: AttributedRow<T>) {
        ops.beginRow(rowContext)
        renderRowCells(state, rowContext)
        ops.endRow(rowContext)
    }
}

fun <T, O> Publisher<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit) {
    if (output is Processor<*, *>) {
        TODO("Not implemented stream composition")
    } else {
        TableExportTemplate<T, O, FlushingRenderingContext<O>>(format).export(
            this,
            SubscribingTabulationHandler(output),
            table(block))
    }
}

fun <T, O> TableBuilder<T>.export(format: TabulationFormat, output: O) {
    TableExportTemplate<T, O, FlushingRenderingContext<O>>(format).export(
        emptyList(),
        IteratingTabulationHandler(output),
        this
    )
}

fun <T> TableBuilder<T>.export(file: File) {
    val ext = file.extension
    if (ext.isNotBlank()) {
        FileOutputStream(file).use {
            TableExportTemplate<T, OutputStream, FlushingRenderingContext<OutputStream>>(TabulationFormat(file.extension)).export(
                emptyList(),
                IteratingTabulationHandler(it),
                this
            )
        }
    } else error("Cannot resolve tabulation format")
}

fun <T> TableBuilder<T>.export(fileName: String) = export(File(fileName))

fun <T, O> Iterable<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit) {
    TableExportTemplate<T, O, FlushingRenderingContext<O>>(format).export(this,
        IteratingTabulationHandler(output),
        table(block))
}

fun <T> Iterable<T>.tabulate(file: File, block: TableBuilderApi<T>.() -> Unit) {
    val ext = file.extension
    if (ext.isNotBlank()) {
        FileOutputStream(file).use {
            TableExportTemplate<T, FileOutputStream, FlushingRenderingContext<FileOutputStream>>(TabulationFormat(file.extension)).export(
                this,
                IteratingTabulationHandler(it),
                table(block)
            )
        }
    } else error("Cannot resolve tabulation format")
}

fun <T> Iterable<T>.tabulate(fileName: String, block: TableBuilderApi<T>.() -> Unit) = tabulate(File(fileName), block)