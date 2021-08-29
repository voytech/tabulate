package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.model.ColumnDef
import io.github.voytech.tabulate.model.NextId
import io.github.voytech.tabulate.template.context.*
import io.github.voytech.tabulate.template.context.AttributedColumnFactory.createAttributedColumn
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.ResultProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.template.spi.Identifiable
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * [TabulationApi] provides small set of methods from [TabulationTemplate] exposed for [TabulationHandler]
 * in order to orchestrate rendering task. Currently rendering can be arranged using:
 * - [IteratingTabulationHandler]
 * - [SubscribingTabulationHandler]
 *
 * @author Wojciech Mąka
 */
interface TabulationApi<T,O> {

    /**
     * To be called explicitly in order to trigger next row rendering.
     * Notice that record used in parameter list is not always the record being currently rendered. It is
     * first buffered and rendered eventually - according to row qualification rules defined trough table DSL api.
     * @param record - a record from source collection to be buffered, and transformed into [RowContext] at some point of time.
     */
    fun nextRow(record: T)

    /**
     * To be called explicitly in order to finalize rendering.
     * Internally finalization consists of following steps:
     * - rendering of all remaining user defined and buffered rows.
     * - rendering of all remaining user defined rows to be rendered after last collection record.
     * Used for rendering of all trailing rows, after there are no records in source collection.
     */
    fun finish()

    /**
     * To be called explicitly in order to flush rendered table into output.
     * Internally it uses compatible result provider to copy state from context into output.
     *
     */
    fun flush(output: O)
}

/**
 * An entry point for data exporting.
 * Contains convenience extension methods:
 * - [TableBuilder.export] for exporting fully user defined table.
 * - [Iterable.tabulate] for tabulating collection of objects. The process is called 'tabulate' to emphasize
 * its behaviour - taking a source and making a table from it.
 * @author Wojciech Mąka
 */
class TabulationTemplate<T>(private val format: TabulationFormat) {

    private val provider: ExportOperationsProvider<T, out RenderingContext> by lazy {
        resolveExportOperationsFactory(format)
    }

    private val ops: TableExportOperations<T> by lazy {
        provider.createExportOperations()
    }

    private val resultProviders: List<ResultProvider<*>> by lazy {
        provider.createResultProviders()
    }

    inner class TabulationApiImpl<O>(private val state: TabulationState<T>) : TabulationApi<T,O> {

        private val resultProvider : ResultProvider<O> by lazy {
            resolveResultProvider()
        }

        override fun nextRow(record: T) = bufferRecordAndRenderRow(state, record)

        override fun finish() {
            renderRemainingBufferedRows(state)
            renderTrailingCustomRows(state)
            renderColumns(state, ColumnRenderPhase.AFTER_LAST_ROW)
            ops.finish()
        }

        override fun flush(output: O) {
            resultProvider.flush(output)
        }
    }

    private fun prepare(tableBuilder: TableBuilder<T>): TabulationState<T> {
        return ops.createTable(tableBuilder).let { table ->
            TabulationState(
                tableModel = table,
                tableName = table.name ?: "table${NextId.nextId()}",
                firstRow = table.firstRow,
                firstColumn = table.firstColumn
            )
        }.also {
            renderColumns(it, ColumnRenderPhase.BEFORE_FIRST_ROW)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveExportOperationsFactory(id: Identifiable): ExportOperationsProvider<T, out RenderingContext> {
        val loader: ServiceLoader<ExportOperationsProvider<*, *>> =
            ServiceLoader.load(ExportOperationsProvider::class.java)
        return loader.filterIsInstance<ExportOperationsProvider<T, out RenderingContext>>().find { it.test(id) }!!
    }

    private inline fun <reified RES : ResultProvider<*>> resolveResultProvider(): RES =
        resultProviders.filterIsInstance<RES>().first()


    /**
     * Performs actual export.
     *
     * @param source iterable collection of objects
     * @param output an output binding.
     * @param tableBuilder [TableBuilderApi] a top level table DSL builder which defines table appearance.
     */
    fun <O> export(
        source: Iterable<T>,
        output: O,
        tableBuilder: TableBuilder<T>,
    ) {
        create<O>(tableBuilder).let { api ->
            source.forEach { api.nextRow(it) }
                .also { api.finish() }
                .also { api.flush(output) }
        }
    }

    /**
     * Returns [TabulationApi] which enables 'interactive' export.
     *
     * @param tableBuilder [TableBuilderApi] a top level table DSL builder which defines table appearance.
     * @return [TabulationApi] which enables 'interactive' export.
     */
    fun <O> create(tableBuilder: TableBuilder<T>): TabulationApi<T, O> {
        ops.initialize()
        return TabulationApiImpl(prepare(tableBuilder))
    }

    private fun bufferRecordAndRenderRow(state: TabulationState<T>, record: T) {
        state.bufferAndNext(record)?.let { renderRow(state, it) }
    }

    private fun renderRemainingBufferedRows(state: TabulationState<T>) {
        do {
            val context = state.next()?.let {
                renderRow(state, it)
                it
            }
        } while (context != null)
    }

    private fun renderTrailingCustomRows(state: TabulationState<T>) {
        state.mark(IndexLabel.TRAILING_ROWS)
        renderRemainingBufferedRows(state)
    }

    private fun renderColumns(state: TabulationState<T>, renderPhase: ColumnRenderPhase) {
        state.tableModel.forEachColumn { columnIndex: Int, column: ColumnDef<T> ->
            ops.renderColumn(
                createAttributedColumn(
                    state.tableModel.getColumnIndex(column.index ?: columnIndex),
                    renderPhase,
                    column.columnAttributes,
                    state.getCustomAttributes()
                )
            )
        }
    }

    private fun renderRowCells(state: TabulationState<T>, context: AttributedRow<T>) {
        state.tableModel.forEachColumn { column: ColumnDef<T> ->
            if (context.rowCellValues.containsKey(column.id)) {
                ops.renderRowCell(context.rowCellValues[column.id]!!)
            }
        }
    }

    private fun renderRow(state: TabulationState<T>, rowContext: AttributedRow<T>) {
        ops.beginRow(rowContext)
        renderRowCells(state, rowContext)
        ops.endRow(rowContext)
    }
}

/**
 * Extension function invoked on a [TableBuilder], which takes [TabulationFormat] and output handler
 *
 * @param format identifier of [ExportOperationsProvider] to export table to specific file format (xlsx, pdf).
 * @param output reference to any kind of output - may be e.g. OutputStream.
 * @receiver DSL top level table builder.
 */
fun <T, O> TableBuilder<T>.export(format: TabulationFormat, output: O) {
    TabulationTemplate<T>(format).export(emptyList(), output, this)
}

/**
 * Extension function invoked on a [TableBuilder], which takes output [file].
 *
 * @param file A [File].
 * @receiver DSL top level table builder.
 */
fun <T> TableBuilder<T>.export(file: File) {
    val ext = file.extension
    if (ext.isNotBlank()) {
        FileOutputStream(file).use {
            TabulationTemplate<T>(TabulationFormat(file.extension)).export(emptyList(), it, this)
        }
    } else error("Cannot resolve tabulation format")
}

/**
 * Extension function invoked on a [TableBuilder], which takes [fileName].
 *
 * @param fileName A path of an output file.
 * @receiver DSL top level table builder.
 */
fun <T> TableBuilder<T>.export(fileName: String) = export(File(fileName))

/**
 * Extension function invoked on a collection, which takes [TabulationFormat], output handler and DSL table builder to define table appearance.
 *
 * @param format explicit identifier of [ExportOperationsProvider] to be used in order to export table to specific file format (xlsx, pdf).
 * @param output reference to any kind of output or sink - may be e.g. OutputStream.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T, O> Iterable<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit) {
    TabulationTemplate<T>(format).export(this, output, table(block))
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
            TabulationTemplate<T>(TabulationFormat(file.extension)).export(this, it, table(block))
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