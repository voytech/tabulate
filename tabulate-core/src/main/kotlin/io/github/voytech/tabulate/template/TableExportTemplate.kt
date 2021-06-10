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

/**
 * An [TableExportTemplate] API exposed for [TabulationHandler] allowing to choose between iteration based or reactive rendering.
 *
 * @author Wojciech Mąka
 */
interface TableExportTemplateApi<T> {
    /**
     * To be called explicitly by [TabulationHandler] implementation in order to initialize rendering.
     */
    fun begin()

    /**
     * To be called explicitly by [TabulationHandler] implementation in order to trigger next row rendering.
     */
    fun nextRow(record: T)

    /**
     * To be called explicitly by [TabulationHandler] implementation in order to finalize rendering.
     * Internally finalization consists of following steps:
     * - rendering of all remaining user defined and buffered rows.
     * - rendering of all remaining user defined rows after size of data source plus user defined rows is known at completion. Used for footer and all closing rows.
     */
    fun end()
}

/**
 * An entry point for exporting.
 *
 * Role of this class is to orchestrate data exporting with [TabulationHandler]s and bootstrap rendering.
 * Contains different sorts of convenience extension methods:
 * - [TableBuilder.export] for exporting fully user-defined table.
 * - [Iterable.tabulate] for tabulating collections of elements. Here the process is called tabulate in order to emphasize
 * on specific behaviour - its a function which turns collection into tabular form.
 * - [Publisher.tabulate] same as above but in reactive manner.
 *
 * @author Wojciech Mąka
 */
class TableExportTemplate<T, O, CTX : RenderingContext>(private val format: TabulationFormat) {

    private val provider: ExportOperationsProvider<T, CTX> by lazy {
        resolveExportOperationsFactory(format)
    }

    private val ops: TableExportOperations<T> by lazy {
        provider.create()
    }

    inner class TableExportTemplateApiImpl(private val state: TableExportingState<T>) : TableExportTemplateApi<T> {

        override fun begin() = renderColumns(state, ColumnRenderPhase.BEFORE_FIRST_ROW)

        override fun nextRow(record: T) = bufferRecordAndRenderRow(state, record)

        override fun end() {
            renderRemainingBufferedRows(state)
            renderRowsAfterDataProcessed(state)
            renderColumns(state, ColumnRenderPhase.AFTER_LAST_ROW)
            ops.finish()
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
    private fun resolveExportOperationsFactory(id: Identifiable): ExportOperationsProvider<T, CTX> {
        val loader: ServiceLoader<ExportOperationsProvider<*, *>> =
            ServiceLoader.load(ExportOperationsProvider::class.java)
        return loader.find { it.test(id) } as ExportOperationsProvider<T, CTX>
    }

    /**
     * Takes any supported source type (e.g. [Iterable], [Publisher] or other), [TabulationHandler] which orchestrates process and DSL top level table builder api [TableBuilderApi] which defines table appearance.
     *
     * @param source any supported data source class (e.g. [Iterable], [Publisher] or other, even more complex class)
     * @param handler [TabulationHandler] orchestrator of table rendering process. Allows to orchestrate rendering in iteration driven - or in reactive manner.
     * @param tableBuilder [TableBuilderApi] a top level table DSL builder which defines table appearance.
     */
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

/**
 * Extension function invoked on a [Publisher], which takes [TabulationFormat], output handler and DSL top level table builder api [TableBuilderApi]
 *
 * @param format explicit identifier of [ExportOperationsProvider] to be used in order to export table to specific file format (xlsx, pdf).
 * @param output reference to any kind of output or sink - may be e.g. OutputStream or Flux.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver DSL top level table builder.
 */
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

/**
 * Extension function invoked on a [TableBuilder], which takes [TabulationFormat], output handler
 *
 * @param format explicit identifier of [ExportOperationsProvider] to be used in order to export table to specific file format (xlsx, pdf).
 * @param output reference to any kind of output or sink - may be e.g. OutputStream or Flux.
 * @receiver DSL top level table builder.
 */
fun <T, O> TableBuilder<T>.export(format: TabulationFormat, output: O) {
    TableExportTemplate<T, O, FlushingRenderingContext<O>>(format).export(
        emptyList(),
        IteratingTabulationHandler(output),
        this
    )
}

/**
 * Extension function invoked on a [TableBuilder], which takes [file] in order to define table appearance.
 *
 * @param file A [File].
 * @receiver DSL top level table builder.
 */
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

/**
 * Extension function invoked on a [TableBuilder], which takes [fileName] in order to define table appearance.
 *
 * @param fileName A path of an output file.
 * @receiver DSL top level table builder.
 */
fun <T> TableBuilder<T>.export(fileName: String) = export(File(fileName))

/**
 * Extension function invoked on a collection of records, which takes [TabulationFormat], output handler and DSL table builder to define table appearance.
 *
 * @param format explicit identifier of [ExportOperationsProvider] to be used in order to export table to specific file format (xlsx, pdf).
 * @param output reference to any kind of output or sink - may be e.g. OutputStream or Flux.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T, O> Iterable<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit) {
    TableExportTemplate<T, O, FlushingRenderingContext<O>>(format).export(this,
        IteratingTabulationHandler(output),
        table(block))
}

/**
 * Extension function invoked on a collection of records, which takes [File] as argument and DSL table builder to define table appearance.
 *
 * @param file a file name to create.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
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

/**
 * Extension function invoked on a collection of records, which takes [fileName] as argument and DSL table builder to define table appearance.
 *
 * @param fileName a file name to create.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T> Iterable<T>.tabulate(fileName: String, block: TableBuilderApi<T>.() -> Unit) = tabulate(File(fileName), block)