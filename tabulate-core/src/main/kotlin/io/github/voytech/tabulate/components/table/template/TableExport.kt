package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.createTable
import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.layout.GridLayoutPolicy
import io.github.voytech.tabulate.core.template.operation.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

typealias StandaloneTableTemplate<T> = StandaloneExportTemplate<Table<T>>


/**
 * An entry point of table export.
 * Contains entry point methods:
 * - [TableExport.doExport] - export collection of objects.
 * - [TableExport.create] - create [TabulationApi] to enable 'interactive' export. Removes restriction of exporting
 * only iterables. Gives full control on when to schedule next item for rendering.
 *
 * This class provides also convenience extension methods:
 * - [TableBuilderState.export] for exporting user defined table.
 * - [Iterable.tabulate] for tabulating collection of elements. Method is called 'tabulate' to emphasize
 * its sole role - constructing tables from various objects.
 * @author Wojciech Mąka
 */

internal class TableExport<T : Any>(
    private val exportContext: ModelExportContext<Table<T>>,
    private val policy: GridLayoutPolicy,
    dataSource: Iterable<T>?,
    private val renderingContext: RenderingContext = exportContext.renderingContext,
) {

    data class ColumnContextAttributes(val start: Attributes, val end: Attributes)

    private class CaptureRowCompletionImpl<T, R : RenderingContext>(
        private val renderingContext: R,
        private val operations: Operations<R>,
    ) : CaptureRowCompletion<T> {

        override fun onRowStartResolved(row: RowStart): OperationResult? = operations(renderingContext, row)

        override fun onCellResolved(cell: CellContext): OperationResult? = operations(renderingContext, cell)

        override fun onRowEndResolved(row: RowEnd<T>): OperationResult? = operations(renderingContext, row)
    }

    private lateinit var rowContextResolver: AccumulatingRowContextResolver<T>

    private lateinit var rowContextIterator: RowContextIterator<T>

    private val overflowOffsets = OverflowOffsets()

    private var remainingRecords: Iterable<T>? = dataSource

    private lateinit var operations: Operations<RenderingContext>

    private val columnContextAttributes = exportContext.model.distributeAttributesForContexts(
        ColumnStart::class.java, ColumnEnd::class.java
    )

    private val columnAttributes: Map<ColumnDef<T>, ColumnContextAttributes> =
        exportContext.model.columns.associateWith { column ->
            column.distributeAttributesForContexts(ColumnStart::class.java, ColumnEnd::class.java).let {
                ColumnContextAttributes(
                    columnContextAttributes.get<ColumnStart>() + it.get<ColumnStart>(),
                    columnContextAttributes.get<ColumnEnd>() + it.get<ColumnEnd>()
                )
            }
        }

    private fun cropDataSource(): Iterable<T>? = with(overflowOffsets) {
        remainingRecords?.crop().also { remainingRecords = it }
    }

    private fun setup(ops: Operations<RenderingContext>) {
        operations = ops
        rowContextResolver = AccumulatingRowContextResolver(
            exportContext.model, exportContext.customStateAttributes, overflowOffsets,
            CaptureRowCompletionImpl(renderingContext, operations)
        )
        rowContextIterator = RowContextIterator(rowContextResolver, overflowOffsets, exportContext)
    }

    /**
     * Captures next element to be rendered at some point of time.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    private fun capture(record: T): ContextResult<RowEnd<T>>? {
        rowContextResolver.append(record)
        return next()
    }

    /**
     * Resolves next element.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    private fun next(): ContextResult<RowEnd<T>>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

    private fun start() {
        with(exportContext) {
            operations(renderingContext, model.asTableStart(exportContext.getCustomAttributes()))
            renderingContext.renderColumnStarts(columnAttributes)
        }
    }

    private fun renderRemainingBufferedRows() {
        if (exportContext.isYOverflow()) return
        @Suppress("ControlFlowWithEmptyBody")
        while (next()?.let { it is SuccessResult } == true);
    }

    private fun RenderingContext.renderColumnStarts(
        columnAttributes: Map<ColumnDef<T>, ColumnContextAttributes>,
    ): OperationResult? = with(exportContext) {
        val iterator = with(overflowOffsets) { model.columns.crop().iterator() }
        var status: OperationResult? = Success
        var column: ColumnDef<T>? = null
        while (iterator.hasNext() && status == Success) {
            column = iterator.next()
            val context = column.asColumnStart(
                model, columnAttributes[column]?.start ?: Attributes(), getCustomAttributes()
            )
            status = operations.invoke(this@renderColumnStarts, context)
        }
        if (status.isXOverflow()) {
            exportContext.suspendX()
            overflowOffsets.setNextIndexOnX(column?.index ?: 0)
        }
        return status
    }

    private fun RenderingContext.renderColumnEnds(columnAttributes: Map<ColumnDef<T>, ColumnContextAttributes>) =
        with(exportContext) {
            model.columns.forEach { column: ColumnDef<T> ->
                operations(
                    this@renderColumnEnds,
                    column.asColumnEnd(model, columnAttributes[column]?.end ?: Attributes(), getCustomAttributes())
                )
            }
        }

    private fun finish() {
        renderRemainingBufferedRows()
        renderingContext.renderColumnEnds(columnAttributes)
        operations(renderingContext, exportContext.model.asTableEnd(exportContext.getCustomAttributes()))
    }

    @Suppress("ControlFlowWithEmptyBody")
    private fun processRows() = remainingRecords?.iterator()?.let { iterator ->
        while (iterator.hasNext() && capture(iterator.next()) !is OverflowResult) {
        }
    }

    fun exportOrResume() = with(exportContext) {
        overflowOffsets.align()
        policy.setOffsets(overflowOffsets.getIndexValueOnY(), overflowOffsets.getIndexOnX())
        setup(instance.getExportOperations(model))
        cropDataSource()
        start()
        processRows()
        finish()
        overflowOffsets.save(exportContext.status)
    }


    //TODO in case of table it is required first pass to measure column and row widths.
    //TODO TableTemplate should be aware of multiple passes and be able to cache some computations.
    fun takeMeasures() = with(exportContext) {
        setup(instance.getMeasuringOperations(model))
        start()
        processRows()
        finish()
        overflowOffsets.reset()
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

