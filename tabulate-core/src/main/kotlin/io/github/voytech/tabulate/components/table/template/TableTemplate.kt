package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.createTable
import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.DataSourceBinding
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.layout.TableLayoutQueries
import io.github.voytech.tabulate.core.template.operation.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

typealias StandaloneTableTemplate<T> = StandaloneExportTemplate<TableTemplate<T>,Table<T>,TableTemplateContext<T>>
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
    fun exportNextRecord(record: T): ContextResult<RowEnd<T>>?

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
 * - [TableTemplate.doExport] - export collection of objects.
 * - [TableTemplate.create] - create [TabulationApi] to enable 'interactive' export. Removes restriction of exporting
 * only iterables. Gives full control on when to schedule next item for rendering.
 *
 * This class provides also convenience extension methods:
 * - [TableBuilderState.export] for exporting user defined table.
 * - [Iterable.tabulate] for tabulating collection of elements. Method is called 'tabulate' to emphasize
 * its sole role - constructing tables from various objects.
 * @author Wojciech Mąka
 */

class TableTemplate<T : Any> : ExportTemplate<TableTemplate<T>, Table<T>, TableTemplateContext<T>>() {

    data class ColumnContextAttributes(val start: Attributes, val end: Attributes)

    private inner class TabulationApiImpl(
        private val renderingContext: RenderingContext,
        private val templateContext: TableTemplateContext<T>,
        private val operations: Operations<RenderingContext>,
    ) : TabulationApi<T> {
        private val columnContextAttributes = templateContext.model.distributeAttributesForContexts(
            ColumnStart::class.java, ColumnEnd::class.java
        )
        private val columnAttributes: Map<ColumnDef<T>, ColumnContextAttributes> =
            templateContext.model.columns.associateWith { column ->
                column.distributeAttributesForContexts(ColumnStart::class.java, ColumnEnd::class.java).let {
                    ColumnContextAttributes(
                        columnContextAttributes.get<ColumnStart>() + it.get<ColumnStart>(),
                        columnContextAttributes.get<ColumnEnd>() + it.get<ColumnEnd>()
                    )
                }
            }

        init {
            with(templateContext) {
                render(model.asTableStart(templateContext.getCustomAttributes()))
                renderingContext.renderColumnStarts(columnAttributes, operations, templateContext)
            }
        }

        override fun exportNextRecord(record: T): ContextResult<RowEnd<T>>? =
            captureRecordAndRenderRow(templateContext, record)

        override fun finish() {
            renderRemainingBufferedRows(templateContext)
            renderingContext.renderColumnEnds(columnAttributes, operations, templateContext)
            operations.render(
                renderingContext,
                templateContext.model.asTableEnd(templateContext.getCustomAttributes())
            )
        }
    }

    private inner class CaptureRowCompletionImpl<T, R : RenderingContext>(
        private val renderingContext: R,
        private val operations: Operations<R>,
    ) : CaptureRowCompletion<T> {

        override fun onRowStartResolved(row: RowStart): OperationStatus? =
            operations.render(renderingContext, row)

        override fun onCellResolved(cell: CellContext): OperationStatus? =
            operations.render(renderingContext, cell)

        override fun onRowEndResolved(row: RowEnd<T>): OperationStatus? =
            operations.render(renderingContext, row)

    }

    private fun <T : Any> captureRecordAndRenderRow(
        state: TableTemplateContext<T>,
        record: T,
    ): ContextResult<RowEnd<T>>? = state.capture(record)

    private fun <T : Any> renderRemainingBufferedRows(state: TableTemplateContext<T>) {
        if (state.isYOverflow()) return
        @Suppress("ControlFlowWithEmptyBody")
        while (state.next()?.let { it is SuccessResult } == true);
    }

    private fun <T : Any> RenderingContext.renderColumnStarts(
        columnAttributes: Map<ColumnDef<T>, ColumnContextAttributes>,
        operations: Operations<RenderingContext>,
        templateContext: TableTemplateContext<T>,
    ): OperationStatus? = with(templateContext) {
        val iterator = with(templateContext.indices) { model.columns.crop().iterator() }
        var status: OperationStatus? = Success
        var column: ColumnDef<T>? = null
        while (iterator.hasNext() && status == Success) {
            column = iterator.next()
            val context = column.asColumnStart(
                model, columnAttributes[column]?.start ?: Attributes(), getCustomAttributes()
            )
            status = operations.render(this@renderColumnStarts, context)
        }
        if (status.isXOverflow()) {
            templateContext.suspendX()
            templateContext.indices.setNextIndexOnX(column?.index ?: 0)
        }
        return status
    }

    private fun <T : Any> RenderingContext.renderColumnEnds(
        columnAttributes: Map<ColumnDef<T>, ColumnContextAttributes>,
        operations: Operations<RenderingContext>,
        templateContext: TableTemplateContext<T>,
    ) = with(templateContext) {
        model.columns.forEach { column: ColumnDef<T> ->
            operations.render(
                this@renderColumnEnds,
                column.asColumnEnd(model, columnAttributes[column]?.end ?: Attributes(), getCustomAttributes())
            )
        }
    }

    @Suppress("ControlFlowWithEmptyBody")
    private fun Iterable<T>?.exportRows(api: TabulationApiImpl) =
        this?.iterator()?.let { iterator ->
            while (iterator.hasNext() && api.exportNextRecord(iterator.next()) !is OverflowResult) { }
        }

    override fun doExport(templateContext: TableTemplateContext<T>): Unit = with(templateContext) {
        createLayoutScope(TableLayoutQueries()) {
            val operations = services.getOperations(model)
            templateContext.setupRowResolver(CaptureRowCompletionImpl(renderingContext, operations))
            TabulationApiImpl(renderingContext, templateContext, operations).let { api ->
                dataSource.exportRows(api)
                api.finish()
            }
        }
        keepStatus()
    }

    override fun doResume(templateContext: TableTemplateContext<T>) = with(templateContext) {
        beforeResume()
        createLayoutScope(
            TableLayoutQueries(templateContext.indices.getIndexValueOnY(), templateContext.indices.getIndexOnX())
        ) {
            val operations = services.getOperations(model)
            templateContext.setupRowResolver(CaptureRowCompletionImpl(renderingContext, operations))
            TabulationApiImpl(renderingContext, templateContext, operations).let { api ->
                cropDataSource().exportRows(api)
                api.finish()
            }
        }
        keepStatus()
    }

    override fun createTemplateContext(
        parentContext: TemplateContext<*, *>,
        model: Table<T>,
    ): TableTemplateContext<T> {
        val binding = parentContext.stateAttributes["_dataSourceOverride"] as? DataSourceBinding<T>
        return TableTemplateContext(
            model, parentContext.stateAttributes,
            parentContext.services,
            model.dataSource?.dataSource ?: binding?.dataSource
        )
    }


    fun <O : Any> export(format: DocumentFormat, source: Iterable<T>, output: O, table: Table<T>) =
        StandaloneTableTemplate<T>(format).export(table, output, source)
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
    StandaloneTableTemplate<T>(format).export(createTable(block), output, this)
}

fun <T : Any, O : Any> Table<T>.export(format: DocumentFormat, output: O) {
    StandaloneTableTemplate<T>(format).export(this, output, emptyList<T>())
}

fun <T : Any, O : Any> export(
    format: DocumentFormat,
    source: Iterable<T>,
    output: O,
    block: TableBuilderApi<T>.() -> Unit,
) = StandaloneTableTemplate<T>(format).export(createTable(block), output, source)


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
            export(format, emptyList(), it, this)
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

