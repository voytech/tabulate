package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.createTable
import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.rendering.*
import io.github.voytech.tabulate.core.DocumentFormat
import io.github.voytech.tabulate.core.InputParams.Companion.allowMeasureBeforeRender
import io.github.voytech.tabulate.core.InputParams.Companion.params
import io.github.voytech.tabulate.core.StandaloneExportTemplate
import io.github.voytech.tabulate.core.documentFormat
import io.github.voytech.tabulate.core.layout.CrossedAxis
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.layout.impl.TableLayout
import io.github.voytech.tabulate.core.model.DataSourceBinding
import io.github.voytech.tabulate.core.model.ExportApi
import io.github.voytech.tabulate.core.operation.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


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
    private val table: Table<T>,
    private val scope: ExportApi,
    data: Iterable<T>?
) {

    private val dataSource: Iterable<T> =
        data ?: scope.getCustomAttributes().get<DataSourceBinding<T>>("_dataSourceOverride")?.dataSource ?: emptyList()

    data class ColumnContextAttributes(val start: Attributes, val end: Attributes)

    private class CaptureRowCompletionImpl<T>(private val api: ExportApi) : CaptureRowCompletion<T> {

        override fun onRowStartResolved(row: RowStartRenderable): RenderingResult = api.renderOrMeasure(row)

        override fun onCellResolved(cell: CellRenderable): RenderingResult = api.renderOrMeasure(cell)

        override fun onRowEndResolved(row: RowEndRenderable<T>): RenderingResult = api.renderOrMeasure(row)
    }

    private lateinit var rowContextResolver: AccumulatingRowContextResolver<T>

    private lateinit var rowContextIterator: RowContextIterator<T>

    private val tableIterations = TableRenderIterations(scope.continuations())

    private var dataSourceActiveWindow: Iterable<T>? = null

    private val columnContextAttributes = table.distributeAttributesForContexts(
        ColumnStartRenderable::class.java, ColumnEndRenderable::class.java
    )

    private val columnAttributes: Map<ColumnDef<T>, ColumnContextAttributes> =
        table.columns.associateWith { column ->
            column.distributeAttributesForContexts(ColumnStartRenderable::class.java, ColumnEndRenderable::class.java)
                .let {
                    ColumnContextAttributes(
                        columnContextAttributes.get<ColumnStartRenderable>() + it.get<ColumnStartRenderable>(),
                        columnContextAttributes.get<ColumnEndRenderable>() + it.get<ColumnEndRenderable>()
                    )
                }
        }

    private fun adjustDataSourceRenderWindow(): Iterable<T>? = with(tableIterations) {
        dataSourceActiveWindow = dataSource.adjustRenderWindow()
        return dataSourceActiveWindow
    }

    private fun setup() {
        rowContextResolver = AccumulatingRowContextResolver(
            table, scope.getCustomAttributes(), tableIterations,
            CaptureRowCompletionImpl(scope)
        )
        rowContextIterator = RowContextIterator(rowContextResolver, tableIterations)
    }

    /**
     * Captures next element to be rendered at some point of time.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    private fun pushAndNext(record: T): ContextResult<RowEndRenderable<T>>? {
        rowContextResolver.append(record)
        return next()
    }

    /**
     * Resolves next element.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    private fun next(): ContextResult<RowEndRenderable<T>>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

    private fun beginTable() {
        scope.renderOrMeasure(table.asTableStart(scope.getCustomAttributes()))
        renderColumnsStarts()
    }

    private fun resolveActiveColumns(): Iterator<ColumnDef<T>> =
        with(tableIterations) { table.columns.adjustRenderWindow().iterator() }

    private fun Map<ColumnDef<T>, ColumnContextAttributes>.forStart(def: ColumnDef<T>): Attributes =
        this[def]?.start ?: Attributes()

    private fun Map<ColumnDef<T>, ColumnContextAttributes>.forEnd(def: ColumnDef<T>): Attributes =
        this[def]?.end ?: Attributes()

    private fun renderColumns(
        addContinuation: Boolean = false, resolveRenderable: (ColumnDef<T>) -> ColumnRenderable,
    ): RenderingStatus? {
        var status: RenderingStatus? = Ok
        resolveActiveColumns().let { iterator ->
            while (iterator.hasNext()) {
                val def = iterator.next()
                status = scope.renderOrMeasure(resolveRenderable(def)).status
                if (status.isSkipped(CrossedAxis.X)) {
                    if (addContinuation) tableIterations.pushNewIteration(def)
                    break
                }
            }
        }
        return status
    }

    private fun renderColumnsStarts(): RenderingStatus? =
        renderColumns(true) { it.asColumnStart(table, columnAttributes.forStart(it), scope.getCustomAttributes()) }


    private fun renderColumnEnds(): RenderingStatus? =
        renderColumns { it.asColumnEnd(table, columnAttributes.forEnd(it), scope.getCustomAttributes()) }

    private fun endTable() {
        renderColumnEnds()
        scope.renderOrMeasure(table.asTableEnd(scope.getCustomAttributes()))
    }

    private fun renderRemainingRows() {
        @Suppress("ControlFlowWithEmptyBody")
        while (next()?.let { it is SuccessResult } == true);
    }

    private fun renderRows() {
        dataSourceActiveWindow?.iterator()?.let { iterator ->
            while (iterator.hasNext()) {
                if (pushAndNext(iterator.next()) is OverflowResult) {
                    return
                }
            }
        }
        renderRemainingRows()
    }

    private fun setTableLayoutPolicyProgress() {
        val startFromColumnIndex = tableIterations.getStartColumnIndexOrZero()
        val startFromRowIndex = tableIterations.getStartRowIndexOrZero()
        scope.currentLayoutScope()
            .layout<TableLayout>()
            .setOffsets(startFromRowIndex, startFromColumnIndex)
    }

    fun renderTable() {
        setTableLayoutPolicyProgress()
        setup()
        adjustDataSourceRenderWindow()
        beginTable()
        renderRows()
        endTable()
    }

    fun <O : Any> export(format: DocumentFormat, source: Iterable<T>, output: O, table: Table<T>) =
        StandaloneExportTemplate(format).export(table, output, source)
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
    StandaloneExportTemplate(format).export(createTable(block), output, this, params().allowMeasureBeforeRender(false))
}

fun <T : Any, O : Any> Table<T>.export(format: DocumentFormat, output: O) {
    StandaloneExportTemplate(format).export(this, output, emptyList<T>(), params().allowMeasureBeforeRender(false))
}

fun <T : Any, O : Any> export(
    format: DocumentFormat,
    source: Iterable<T>,
    output: O,
    block: TableBuilderApi<T>.() -> Unit,
) = StandaloneExportTemplate(format).export(
    createTable(block),
    output,
    source,
    params().allowMeasureBeforeRender(false)
)


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

