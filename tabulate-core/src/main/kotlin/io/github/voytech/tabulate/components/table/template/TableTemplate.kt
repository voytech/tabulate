package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.createTable
import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.DataSourceBinding
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.layout.TableLayoutQueries
import io.github.voytech.tabulate.core.template.operation.Operations
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
interface TabulationApi<T> {

    /**
     * To be called explicitly in order to trigger next row rendering.
     * Notice that record used in parameter list is not always the record being currently rendered. It is
     * first buffered and rendered eventually according to row qualification rules defined trough table DSL api.
     * @param record - a record from source collection to be buffered, and transformed into [RowEnd] at some point of time.
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

}

/**
 * An entry point of table export.
 * Contains entry point methods:
 * - [TableTemplate.export] - export collection of objects.
 * - [TableTemplate.create] - create [TabulationApi] to enable 'interactive' export. Removes restriction of exporting
 * only iterables. Gives full control on when to schedule next item for rendering.
 *
 * This class provides also convenience extension methods:
 * - [TableBuilderState.export] for exporting user defined table.
 * - [Iterable.tabulate] for tabulating collection of elements. Method is called 'tabulate' to emphasize
 * its sole role - constructing tables from from various objects.
 * @author Wojciech Mąka
 */

class TableTemplate<T : Any> : ExportTemplate<Table<T>, TableTemplateContext<T>> {

    private inner class TabulationApiImpl<T : Any, R : RenderingContext>(
        private val renderingContext: R,
        private val templateContext: TableTemplateContext<T>,
        private val operations: Operations<R>,
    ) : TabulationApi<T> {

        init {
            with(templateContext) {
                operations.render(renderingContext, model.asTableStart(templateContext.getCustomAttributes()))
                renderColumns(renderingContext, operations, templateContext, ColumnRenderPhase.BEFORE_FIRST_ROW)
            }
        }

        override fun nextRow(record: T) = captureRecordAndRenderRow(templateContext, record)

        override fun finish() {
            renderRemainingBufferedRows(templateContext)
            renderColumns(renderingContext, operations, templateContext, ColumnRenderPhase.AFTER_LAST_ROW)
            operations.render(
                renderingContext,
                templateContext.model.asTableEnd(templateContext.getCustomAttributes())
            )
        }
    }

    private inner class RowCompletionListenerImpl<T, R : RenderingContext>(
        private val renderingContext: R,
        private val operations: Operations<R>,
    ) : RowCompletionListener<T> {

        override fun onAttributedRowResolved(row: RowStart) {
            operations.render(renderingContext, row)
        }

        override fun onAttributedCellResolved(cell: CellContext) {
            operations.render(renderingContext, cell)
        }

        override fun onAttributedRowResolved(row: RowEnd<T>) {
            operations.render(renderingContext, row)
        }

    }

    private fun <T : Any> captureRecordAndRenderRow(state: TableTemplateContext<T>, record: T) {
        state.capture(record)
    }

    private fun <T : Any> renderRemainingBufferedRows(state: TableTemplateContext<T>) {
        @Suppress("ControlFlowWithEmptyBody")
        while (state.next() != null);
    }

    private fun <R : RenderingContext, T : Any> renderColumns(
        renderingContext: R,
        operations: Operations<R>,
        templateContext: TableTemplateContext<T>,
        renderPhase: ColumnRenderPhase,
    ) {
        with(templateContext) {
            model.columns.forEach { column: ColumnDef<T> ->
                operations.render(
                    renderingContext,
                    when (renderPhase) {
                        ColumnRenderPhase.BEFORE_FIRST_ROW ->
                            model.asColumnStart(column, getCustomAttributes())
                        ColumnRenderPhase.AFTER_LAST_ROW ->
                            model.asColumnEnd(column, getCustomAttributes())
                    }
                )
            }
        }
    }

    override fun <R : RenderingContext> export(
        renderingContext: R,
        templateContext: TableTemplateContext<T>,
        apis: ExportTemplateApis<R>,
    ) = with(templateContext) {
        val operations = apis.getOperations(model) ?: error("operations are required by TableTemplate")
        apis.getActiveLayout().newLayout(TableLayoutQueries()).let { layout ->
            templateContext.initializeInternalState(RowCompletionListenerImpl(renderingContext, operations))
            TabulationApiImpl(renderingContext, templateContext, operations).let { api ->
                templateContext.dataSource?.forEach { api.nextRow(it) }
                    .also { api.finish() }
            }
            layout.finish()
        }
    }

    override fun buildTemplateContext(
        parentContext: TemplateContext<*>,
        childModel: Table<T>,
    ): TableTemplateContext<T> {
        val binding = parentContext.stateAttributes["_dataSourceOverride"] as? DataSourceBinding<T>
        return TableTemplateContext(
            childModel, parentContext.stateAttributes,
            childModel.dataSource?.dataSourceRecordClass ?: binding?.dataSourceRecordClass,
            childModel.dataSource?.dataSource ?: binding?.dataSource
        )
    }

    override fun modelClass(): Class<Table<T>> = reify()


    fun <O : Any> export(format: DocumentFormat, source: Iterable<T>, output: O, table: Table<T>) =
        StandaloneExportTemplate(format, this).export(table, output, source)

}

/**
 * Extension function invoked on a collection, which takes [TabulationFormat], output handler and DSL table builder to define table appearance.
 *
 * @param format explicit identifier of [ExportOperationsProvider] to be used in order to export table to specific file format (xlsx, pdf).
 * @param output reference to any kind of output or sink - may be e.g. OutputStream.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T : Any, O : Any> Iterable<T>.tabulate(format: DocumentFormat, output: O, block: TableBuilderApi<T>.() -> Unit) {
    StandaloneExportTemplate(format, TableTemplate<T>()).export(createTable(block), output, this)
}

fun <T : Any, O : Any> Table<T>.export(format: DocumentFormat, output: O) {
    StandaloneExportTemplate(format, TableTemplate<T>()).export(this, output, emptyList<T>())
}

fun <T : Any, O : Any> TableTemplate<T>.export(
    format: DocumentFormat,
    source: Iterable<T>,
    output: O,
    block: TableBuilderApi<T>.() -> Unit,
) =
    StandaloneExportTemplate(format, this).export(createTable(block), output, source)


/**
 * Extension function invoked on a [TableBuilderState], which takes [TabulationFormat] and output handler
 *
 * @param format identifier of [ExportOperationsProvider] to export table to specific file format (xlsx, pdf).
 * @param output output binding - may be e.g. OutputStream.
 * @receiver top level DSL table builder.
 */
fun <T : Any, O : Any> (TableBuilderApi<T>.() -> Unit).export(format: DocumentFormat, output: O) {
    createTable(this).export(format, output)
}

/**
 * Extension function invoked on a [TableBuilderState], which takes output [file].
 *
 * @param file A [File].
 * @receiver top level DSL table builder.
 */
fun <T : Any> (TableBuilderApi<T>.() -> Unit).export(file: File) {
    file.documentFormat().let { format ->
        FileOutputStream(file).use {
            TableTemplate<T>().export(format, emptyList(), it, this)
        }
    }
}

/**
 * Extension function invoked on a [TableBuilderState], which takes [fileName].
 *
 * @param fileName A path of an output file.
 * @receiver top level DSL table builder.
 */
fun <T : Any> (TableBuilderApi<T>.() -> Unit).export(fileName: String) = export(File(fileName))


/**
 * Extension function invoked on a collection of records, which takes [File] and DSL table builder to define table appearance.
 *
 * @param file a file name to create.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T : Any> Iterable<T>.tabulate(file: File, block: TableBuilderApi<T>.() -> Unit) {
    file.documentFormat().let { format ->
        FileOutputStream(file).use {
            tabulate(format, it, block)
        }
    }
}

/**
 * Extension function invoked on a collection of elements, which takes [fileName] and DSL table builder as arguments.
 *
 * @param fileName a file name to create.
 * @param block [TableBuilderApi] a top level table DSL builder which defines table appearance.
 * @receiver collection of records to be rendered into file.
 */
fun <T : Any> Iterable<T>.tabulate(fileName: String, block: TableBuilderApi<T>.() -> Unit) =
    tabulate(File(fileName), block)

