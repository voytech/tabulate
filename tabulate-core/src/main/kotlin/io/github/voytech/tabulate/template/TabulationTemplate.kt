package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.api.builder.TableBuilderTransformer
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.model.ColumnDef
import io.github.voytech.tabulate.model.attributes.overrideAttributesLeftToRight
import io.github.voytech.tabulate.template.context.*
import io.github.voytech.tabulate.template.context.AttributedColumnFactory.createAttributedColumn
import io.github.voytech.tabulate.template.exception.ExportOperationsFactoryResolvingException
import io.github.voytech.tabulate.template.exception.ResultProviderResolvingException
import io.github.voytech.tabulate.template.exception.UnknownTabulationFormatException
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.resolvers.RowCompletionNotifier
import io.github.voytech.tabulate.template.result.ResultProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import java.io.File
import java.io.FileOutputStream
import java.util.*
import io.github.voytech.tabulate.api.builder.fluent.TableBuilder as FluentTableBuilderApi

/**
 * [TabulationApi] exposes an API enabling interactive table export.
 * Particularly it allows to:
 * - export each record respectively with 'nextRow' method,
 * - export all trailing and buffered records with 'finish' method,
 * - flush rendered data into output.
 * @author Wojciech Mąka
 */
interface TabulationApi<T, O> {

    /**
     * To be called explicitly in order to trigger next row rendering.
     * Notice that record used in parameter list is not always the record being currently rendered. It is
     * first buffered and rendered eventually - according to row qualification rules defined trough table DSL api.
     * @param record - a record from source collection to be buffered, and transformed into [RowContextWithCells] at some point of time.
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
     * Internally it uses compatible result provider to copy state from rendering context into output.
     */
    fun flush()
}

/**
 * An entry point for data exporting.
 * Contains core exporting methods:
 * - [TabulationTemplate.export] - export collection of objects.
 * - [TabulationTemplate.create] - create and return [TabulationApi] for 'interactive' exporting.
 * And following convenience extension methods:
 * - [TableBuilder.export] for exporting fully user defined table.
 * - [Iterable.tabulate] for tabulating collection of objects. The process is called 'tabulate' to emphasize
 * its behaviour - taking a source and making a table from it.
 * @author Wojciech Mąka
 */
class TabulationTemplate<T>(private val format: TabulationFormat) {

    private val provider: ExportOperationsProvider<T> by lazy { resolveExportOperationsFactory(format) }

    private val ops: TableExportOperations<T> by lazy { provider.createExportOperations() }

    private val resultProviders: List<ResultProvider<*>> by lazy { provider.createResultProviders() }

    private inner class TabulationApiImpl<O>(
        private val state: TabulationState<T>,
        private val output: O
    ) : TabulationApi<T, O> {

        private val resultProvider: ResultProvider<O> by lazy {
            resolveResultProvider(output)
        }

        init {
            resultProvider.setOutput(output)
            ops.createTable(state.tableModel.createContext(state.getCustomAttributes()))
            renderColumns(state, ColumnRenderPhase.BEFORE_FIRST_ROW)
        }

        override fun nextRow(record: T) = bufferRecordAndRenderRow(state, record)

        override fun finish() {
            renderRemainingBufferedRows(state)
            renderColumns(state, ColumnRenderPhase.AFTER_LAST_ROW)
        }

        override fun flush() {
            resultProvider.flush()
        }
    }

    private inner class RowCompletionNotifierImpl : RowCompletionNotifier<T> {

        override fun beginRow(row: AttributedRow<T>) {
            ops.beginRow(row)
        }

        override fun onCellContextResolved(cell: AttributedCell) {
            ops.renderRowCell(cell)
        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun transform(tableBuilder: TableBuilder<T>): TableBuilder<T> {
        return (resolveBuilderTransformers() + ops.takeIf {
            it is TableBuilderTransformer<*>
        }).filterNotNull()
            .map { it as TableBuilderTransformer<T> }
            .fold(tableBuilder) { builder, transformer -> transformer.transform(builder) }
    }

    private fun materialize(tableBuilder: TableBuilder<T>): TabulationState<T> {
        return transform(tableBuilder).build().let { table ->
            TabulationState(
                tableModel = table,
                tableName = table.name,
                firstRow = table.firstRow,
                firstColumn = table.firstColumn,
                rowCompletionNotifier = RowCompletionNotifierImpl()
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveExportOperationsFactory(format: TabulationFormat): ExportOperationsProvider<T> {
        return ServiceLoader.load(ExportOperationsProvider::class.java)
            .filterIsInstance<ExportOperationsProvider<T>>().find {
                if (format.provider.isNullOrBlank()) {
                    format.id == it.supportsFormat().id
                } else {
                    format == it.supportsFormat()
                }
            } ?: throw ExportOperationsFactoryResolvingException()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <O> resolveResultProvider(output: O): ResultProvider<O> =
        resultProviders.filter {
            it.outputClass().isAssignableFrom(output!!::class.java)
        }.map { it as ResultProvider<O> }
            .firstOrNull() ?: throw ResultProviderResolvingException()

    @Suppress("UNCHECKED_CAST")
    private fun resolveBuilderTransformers(): List<TableBuilderTransformer<T>> {
        return ServiceLoader.load(TableBuilderTransformer::class.java)
            .map { it as TableBuilderTransformer<T> }
            .toList()
    }

    /**
     * Performs actual export.
     *
     * @param source iterable collection of objects
     * @param output an output binding.
     * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
     */
    fun <O> export(
        source: Iterable<T>,
        output: O,
        block: TableBuilderApi<T>.() -> Unit,
    ) {
        create(output, block).let { api ->
            source.forEach { api.nextRow(it) }
                .also { api.finish() }
                .also { api.flush() }
        }
    }

    /**
     * Performs actual export.
     *
     * @param source iterable collection of objects
     * @param output an output binding.
     * @param builder [FluentTableBuilderApi] a top level table fluent builder which defines table appearance.
     */
    fun <O> export(
        source: Iterable<T>,
        output: O,
        builder: FluentTableBuilderApi<T>
    ) {
        create(output, builder).let { api ->
            source.forEach { api.nextRow(it) }
                .also { api.finish() }
                .also { api.flush() }
        }
    }

    /**
     * Returns [TabulationApi] which enables 'interactive' export.
     *
     * @param output output binding.
     * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
     * @return [TabulationApi] which enables 'interactive' export.
     */
    fun <O> create(output: O, block: TableBuilderApi<T>.() -> Unit): TabulationApi<T, O> {
        return TabulationApiImpl(
            materialize(createTableBuilder(block)),
            output
        )
    }

    /**
     * Returns [TabulationApi] which enables 'interactive' export.
     *
     * @param output output binding.
     * @param builder [FluentTableBuilderApi] a top level table fluent builder which defines table appearance.
     * @return [TabulationApi] which enables 'interactive' export.
     */
    fun <O> create(output: O, builder: FluentTableBuilderApi<T>): TabulationApi<T, O> {
        return TabulationApiImpl(
            materialize(builder.builderState),
            output
        )
    }

    private fun bufferRecordAndRenderRow(state: TabulationState<T>, record: T) {
        state.bufferAndNext(record)?.let { ops.endRow(it) }
    }

    private fun renderRemainingBufferedRows(state: TabulationState<T>) {
        do {
            val context = state.next()?.let {
                ops.endRow(it)
                it
            }
        } while (context != null)
    }

    private fun renderColumns(state: TabulationState<T>, renderPhase: ColumnRenderPhase) {
        state.tableModel.forEachColumn { columnIndex: Int, column: ColumnDef<T> ->
            ops.renderColumn(
                createAttributedColumn(
                    state.tableModel.getColumnIndex(column.index ?: columnIndex),
                    renderPhase,
                    overrideAttributesLeftToRight(
                        state.tableModel.columnAttributes,
                        column.columnAttributes
                    ),
                    state.getCustomAttributes()
                )
            )
        }
    }

}

/**
 * Extension function invoked on a [TableBuilder], which takes [TabulationFormat] and output handler
 *
 * @param format identifier of [ExportOperationsProvider] to export table to specific file format (xlsx, pdf).
 * @param output output binding - may be e.g. OutputStream.
 * @receiver top level DSL table builder.
 */
fun <T, O> (TableBuilderApi<T>.() -> Unit).export(format: TabulationFormat, output: O) {
    TabulationTemplate<T>(format).export(emptyList(), output, this)
}

fun File.tabulationFormat(provider: String? = null) =
    if (extension.isNotBlank()) {
        TabulationFormat(extension, provider)
    } else throw UnknownTabulationFormatException()

/**
 * Extension function invoked on a [TableBuilder], which takes output [file].
 *
 * @param file A [File].
 * @receiver top level DSL table builder.
 */
fun <T> (TableBuilderApi<T>.() -> Unit).export(file: File) {
    file.tabulationFormat().let { format ->
        FileOutputStream(file).use {
            TabulationTemplate<T>(format).export(emptyList(), it, this)
        }
    }
}

/**
 * Extension function invoked on a [TableBuilder], which takes [fileName].
 *
 * @param fileName A path of an output file.
 * @receiver top level DSL table builder.
 */
fun <T> (TableBuilderApi<T>.() -> Unit).export(fileName: String) = export(File(fileName))

/**
 * Extension function invoked on a collection, which takes [TabulationFormat], output handler and DSL table builder to define table appearance.
 *
 * @param format explicit identifier of [ExportOperationsProvider] to be used in order to export table to specific file format (xlsx, pdf).
 * @param output reference to any kind of output or sink - may be e.g. OutputStream.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T, O> Iterable<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit) {
    TabulationTemplate<T>(format).export(this, output, block)
}

/**
 * Extension function invoked on a collection of records, which takes [File] as argument and DSL table builder to define table appearance.
 *
 * @param file a file name to create.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T> Iterable<T>.tabulate(file: File, block: TableBuilderApi<T>.() -> Unit) {
    file.tabulationFormat().let { format ->
        FileOutputStream(file).use {
            TabulationTemplate<T>(format).export(this, it, block)
        }
    }
}

/**
 * Extension function invoked on a collection of records, which takes [fileName] as argument and DSL table builder to define table appearance.
 *
 * @param fileName a file name to create.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T> Iterable<T>.tabulate(fileName: String, block: TableBuilderApi<T>.() -> Unit) = tabulate(File(fileName), block)