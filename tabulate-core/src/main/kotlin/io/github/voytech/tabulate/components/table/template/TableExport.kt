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
    private val api: ExportApi,
    private val tableIterations: TableRenderIterations,
    data: Iterable<T>?
) {
    private val dataSource: List<T> =
        data?.toList() ?: api.getCustomAttributes()
            .get<DataSourceBinding<T>>("_dataSourceOverride")?.dataSource?.toList() ?: emptyList()

    private val lastDataSourceIndex = dataSource.size - 1

    private var dataSourceActiveWindow: Iterable<T> = with(tableIterations) { dataSource.adjustRenderWindow() }

    data class ColumnContextAttributes(val start: Attributes, val end: Attributes)

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

    private fun createRowRenderer() =
        TableRowsRenderer(table, dataSourceActiveWindow, api, lastDataSourceIndex, tableIterations)

    private fun beginTable() {
        api.renderOrMeasure(table.asTableStart(api.getCustomAttributes()))
        renderColumnsStarts()
    }

    private fun resolveActiveColumns(): Iterator<ColumnDef<T>> =
        with(tableIterations) { table.columns.adjustRenderWindow().iterator() }

    private fun Map<ColumnDef<T>, ColumnContextAttributes>.forStart(def: ColumnDef<T>): Attributes =
        this[def]?.start ?: Attributes()

    private fun Map<ColumnDef<T>, ColumnContextAttributes>.forEnd(def: ColumnDef<T>): Attributes =
        this[def]?.end ?: Attributes()


    private fun renderColumnsStarts(): RenderingStatus? {
        var status: RenderingStatus? = Ok
        resolveActiveColumns().let { iterator ->
            while (iterator.hasNext()) {
                val def = iterator.next()
                val renderable = def.asColumnStart(table, columnAttributes.forStart(def), api.getCustomAttributes())
                api.renderOrMeasure(renderable).status.also { if (it != Ok) status = it }
            }
        }
        return status
    }

    private fun renderColumnEnds(): RenderingStatus? {
        var status: RenderingStatus? = Ok
        resolveActiveColumns().let { iterator ->
            while (iterator.hasNext()) {
                val def = iterator.next()
                val renderable = def.asColumnEnd(table, columnAttributes.forEnd(def), api.getCustomAttributes())
                status = api.renderOrMeasure(renderable).status
                when {
                    status.isSkipped(CrossedAxis.X) -> {
                        //tableIterations.pushNewIteration(def)
                        //tableIterations.pushNewIteration(def)
                        break
                    }
                    status.isClipped(CrossedAxis.X) -> {
                        //tableIterations.limitWithEndColumn(def)
                        //if (iterator.hasNext()) {
                        //    tableIterations.insertAsNextIteration(iterator.next())
                        //}
                        break
                    }
                }
            }
        }
        return status
    }

    private fun endTable() {
        renderColumnEnds()
        api.renderOrMeasure(table.asTableEnd(api.getCustomAttributes()))
    }

    fun renderTable() {
        createRowRenderer().let {
            beginTable()
            it.renderRows()
            endTable()
        }
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

