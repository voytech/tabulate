package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.TableBuilderState
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.createTable
import io.github.voytech.tabulate.model.ColumnDef
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.context.*
import io.github.voytech.tabulate.template.exception.ExportOperationsFactoryResolvingException
import io.github.voytech.tabulate.template.exception.ResultProviderResolvingException
import io.github.voytech.tabulate.template.exception.UnknownTabulationFormatException
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.resolvers.RowCompletionListener
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * [TabulationApi] An API enabling interactive table export.
 * Allows to:
 * - export each collection element with 'nextRow' method,
 * - export all trailing (custom trailing rows like table footer) and buffered records with 'finish' method,
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
 * An entry point of table export.
 * Contains entry point methods:
 * - [TabulationTemplate.export] - export collection of objects.
 * - [TabulationTemplate.create] - create [TabulationApi] to enable 'interactive' export. Removes restriction of exporting
 * only iterables. Gives full control on when to schedule next item for rendering.
 * And convenience extension methods:
 * - [TableBuilderState.export] for exporting user defined table.
 * - [Iterable.tabulate] for tabulating collection of elements. Method is called 'tabulate' to emphasize
 * its sole role - constructing tables from from various objects.
 * @author Wojciech Mąka
 */
class TabulationTemplate<T>(private val format: TabulationFormat) {

    private val provider: ExportOperationsProvider<RenderingContext> by lazy { resolveExportOperationsFactory(format) }

    private val ops: AttributedContextExportOperations<RenderingContext> by lazy { provider.createExportOperations() }

    private val outputBindings: List<OutputBinding<RenderingContext, *>> by lazy { provider.createOutputBindings() }

    private inner class TabulationApiImpl<O>(
        private val state: TabulationState<T>,
        private val output: O
    ) : TabulationApi<T, O> {

        private val outputBinding: OutputBinding<RenderingContext, O> by lazy {
            resolveOutputBinding(output)
        }

        init {
            outputBinding.setOutput(state.renderingContext, output)
            ops.createTable(state.renderingContext, state.tableModel.createContext(state.getCustomAttributes()))
            renderColumns(state, ColumnRenderPhase.BEFORE_FIRST_ROW)
        }

        override fun nextRow(record: T) = captureRecordAndRenderRow(state, record)

        override fun finish() {
            renderRemainingBufferedRows(state)
            renderColumns(state, ColumnRenderPhase.AFTER_LAST_ROW)
        }

        override fun flush() {
            outputBinding.flush()
        }
    }

    private inner class RowCompletionListenerImpl(private val renderingContext: RenderingContext) :
        RowCompletionListener<T> {

        override fun onAttributedRowResolved(row: AttributedRow) = ops.beginRow(renderingContext, row)

        override fun onAttributedCellResolved(cell: AttributedCell) = ops.renderRowCell(renderingContext, cell)

        override fun onAttributedRowResolved(row: AttributedRowWithCells<T>) = ops.endRow(renderingContext, row)

    }

    private fun materialize(table: Table<T>): TabulationState<T> {
        return provider.createRenderingContext().let { renderingContext ->
            TabulationState(renderingContext, table, RowCompletionListenerImpl(renderingContext))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveExportOperationsFactory(format: TabulationFormat): ExportOperationsProvider<RenderingContext> {
        return ServiceLoader.load(ExportOperationsProvider::class.java)
            .filterIsInstance<ExportOperationsProvider<RenderingContext>>().find {
                if (format.provider.isNullOrBlank()) {
                    format.id == it.supportsFormat().id
                } else {
                    format == it.supportsFormat()
                }
            } ?: throw ExportOperationsFactoryResolvingException()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <O> resolveOutputBinding(output: O): OutputBinding<RenderingContext, O> =
        outputBindings.filter {
            it.outputClass().isAssignableFrom(output!!::class.java)
        }.map { it as OutputBinding<RenderingContext, O> }
            .firstOrNull() ?: throw ResultProviderResolvingException()

    /**
     * Performs actual export.
     *
     * @param source iterable collection of objects
     * @param output an output binding.
     * @param table [Table] a top level table model which defines table appearance.
     */
    fun <O> export(
        source: Iterable<T>,
        output: O,
        table: Table<T>,
    ) {
        create(output, table).let { api ->
            source.forEach { api.nextRow(it) }
                .also { api.finish() }
                .also { api.flush() }
        }
    }

    /**
     * Returns [TabulationApi] which enables 'interactive' export.
     *
     * @param output output binding.
     * @param table a top level table model which defines table appearance.
     * @return [TabulationApi] which enables 'interactive' export.
     */

    fun <O> create(output: O, table: Table<T>): TabulationApi<T, O> {
        return TabulationApiImpl(materialize(table), output)
    }


    private fun captureRecordAndRenderRow(state: TabulationState<T>, record: T) {
        state.capture(record)
    }

    private fun renderRemainingBufferedRows(state: TabulationState<T>) {
        @Suppress("ControlFlowWithEmptyBody")
        while (state.next() != null);
    }

    private fun renderColumns(state: TabulationState<T>, renderPhase: ColumnRenderPhase) {
        with(state) {
            tableModel.columns.forEach { column: ColumnDef<T> ->
                ops.renderColumn(
                    renderingContext,
                    tableModel.createAttributedColumn(
                        column,
                        renderPhase,
                        state.getCustomAttributes()
                    )
                )
            }
        }
    }

}

fun <T, O> TabulationTemplate<T>.export(source: Iterable<T>, output: O, block: TableBuilderApi<T>.() -> Unit) =
    export(source, output, createTable(block))

fun <T,O> Table<T>.export(format: TabulationFormat, output: O) {
    TabulationTemplate<T>(format).export(emptyList(), output, this)
}

/**
 * Extension function invoked on a [TableBuilderState], which takes [TabulationFormat] and output handler
 *
 * @param format identifier of [ExportOperationsProvider] to export table to specific file format (xlsx, pdf).
 * @param output output binding - may be e.g. OutputStream.
 * @receiver top level DSL table builder.
 */
fun <T, O> (TableBuilderApi<T>.() -> Unit).export(format: TabulationFormat, output: O) {
    createTable(this).export(format, output)
}

fun File.tabulationFormat(provider: String? = null) =
    if (extension.isNotBlank()) {
        TabulationFormat(extension, provider)
    } else throw UnknownTabulationFormatException()

/**
 * Extension function invoked on a [TableBuilderState], which takes output [file].
 *
 * @param file A [File].
 * @receiver top level DSL table builder.
 */
fun <T> (TableBuilderApi<T>.() -> Unit).export(file: File) {
    file.tabulationFormat().let { format ->
        FileOutputStream(file).use {
            TabulationTemplate<T>(format).export(emptyList(), it, createTable(this))
        }
    }
}

/**
 * Extension function invoked on a [TableBuilderState], which takes [fileName].
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
    TabulationTemplate<T>(format).export(this, output, createTable(block))
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
            TabulationTemplate<T>(format).export(this, it, createTable(block))
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