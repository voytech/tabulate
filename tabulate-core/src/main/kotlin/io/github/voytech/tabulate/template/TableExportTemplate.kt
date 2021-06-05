package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.model.ColumnDef
import io.github.voytech.tabulate.model.NextId
import io.github.voytech.tabulate.template.context.*
import io.github.voytech.tabulate.template.iterators.OperationContextIterator
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver
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
    fun renderNextRow(record: T)
    fun end(result: O)
}

/**
 * An entry point for exporting. Role of this class is to orchestrate and bind all things together.
 * @author Wojciech Mąka
 */
open class TableExportTemplate<T, O, CTX : RenderingContext>() {

    private lateinit var ops: TableExportOperations<T, O>

    private lateinit var renderingContext: CTX

    constructor(output: TabulationFormat) : this() {
        resolveExportOperationsFactory(output).let {
            this.ops = it.create()
            this.renderingContext = it.getRenderingContext()
        }
    }

    constructor(delegate: TableExportOperations<T, O>, ctx: CTX) : this() {
        this.ops = delegate
        this.renderingContext = ctx
    }

    inner class TableExportTemplateApiImpl(
        private val state: TableExportingState<T>,
        private val resolver: BufferingRowContextResolver<T> = BufferingRowContextResolver(),
        private var iterator: OperationContextIterator<T, AttributedRow<T>> = OperationContextIterator(resolver),
    ) : TableExportTemplateApi<T, O> {

        override fun begin() {
            iterator.setState(state)
            renderColumns(state, ColumnRenderPhase.BEFORE_FIRST_ROW)
        }

        override fun renderNextRow(record: T) {
            resolver.buffer(record)
            renderNextRow()
        }

        override fun end(result: O) {
            renderBufferedRows()
            renderRowsAfterDataProcessed()
            renderColumns(state, ColumnRenderPhase.AFTER_LAST_ROW)
            ops.finish(result)
        }

        private fun renderBufferedRows() {
            while (iterator.hasNext()) {
                renderNextRow()
            }
        }

        private fun renderRowsAfterDataProcessed() {
            state.mark(IndexLabel.DATASET_PROCESSED)
            iterator = OperationContextIterator(resolver).also { it.setState(state) }
            while (iterator.hasNext()) {
                renderNextRow()
            }
        }

        private fun renderNextRow() {
            if (iterator.hasNext()) {
                renderNextRow(state, iterator.next())
            }
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

    fun export(source: Publisher<T>, handler: ReactiveTabulationHandler<T, O, CTX>, tableBuilder: TableBuilder<T>) {
        ops.initialize()
        createTable(tableBuilder).let {
            handler.orchestrate(
                source,
                TableExportTemplateApiImpl(it),
                renderingContext
            )
        }
    }

    fun export(source: Iterable<T>, handler: TabulationHandler<T, O, CTX>, tableBuilder: TableBuilder<T>) {
        ops.initialize()
        createTable(tableBuilder).let {
            handler.orchestrate(
                source,
                TableExportTemplateApiImpl(it),
                renderingContext
            )
        }
    }

    private fun renderColumns(stateAndAttributes: TableExportingState<T>, renderPhase: ColumnRenderPhase) {
        stateAndAttributes.tableModel.forEachColumn { columnIndex: Int, column: ColumnDef<T> ->
            ops.renderColumn(
                stateAndAttributes.createColumnContext(IndexedValue(column.index ?: columnIndex, column), renderPhase)
            )
        }
    }

    private fun renderRowCells(tableExportingState: TableExportingState<T>, context: AttributedRow<T>) {
        tableExportingState.tableModel.forEachColumn { column: ColumnDef<T> ->
            if (context.rowCellValues.containsKey(column.id)) {
                ops.renderRowCell(context.rowCellValues[column.id]!!)
            }
        }
    }

    private fun renderNextRow(tableExportingState: TableExportingState<T>, rowContext: AttributedRow<T>) {
        ops.beginRow(rowContext)
        renderRowCells(tableExportingState, rowContext)
        ops.endRow(rowContext)
    }
}

fun <T, O> Publisher<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit) {
    if (output is Processor<*,*>) {
        TODO("Not implemented stream composition")
    } else {
        TableExportTemplate<T, O, WritableRenderingContext<O>>(format).export(this, BaseSubscribingTabulationHandler(output), table(block))
    }
}

fun <T, O> Iterable<T>.tabulate(format: TabulationFormat, output: O, block: TableBuilderApi<T>.() -> Unit) {
    TableExportTemplate<T, O, WritableRenderingContext<O>>(format).export(this, BaseTabulationHandler(output), table(block))
}

fun <T> Iterable<T>.tabulate(fileName: String, block: TableBuilderApi<T>.() -> Unit) {
    val file = File(fileName)
    val ext = file.extension
    if (ext.isNotBlank()) {
        FileOutputStream(file).use {
            TableExportTemplate<
                    T,
                    FileOutputStream,
                    WritableRenderingContext<FileOutputStream>>(TabulationFormat(file.extension)).export(
                this,
                BaseTabulationHandler(it),
                table(block)
            )
        }
    } else error("Cannot resolve tabulation format")
}

fun <T, O> Publisher<T>.tabulate(
    operations: TableExportOperations<T, O>,
    output: O,
    block: TableBuilderApi<T>.() -> Unit,
) {
   // TableExportTemplate(operations, VoidRenderingContext()).export(this, BaseSubscribingTabulationHandler(output), table(block))
}

fun <T, O> Iterable<T>.tabulate(
    operations: TableExportOperations<T, O>,
    output: O,
    block: TableBuilderApi<T>.() -> Unit,
) {
    TableExportTemplate(operations,VoidRenderingContext()).export(this, NoContextBaseTabulationHandler(output), table(block))
}

fun <T> TableBuilder<T>.export(operations: TableExportOperations<T, OutputStream>, stream: OutputStream) {
    TableExportTemplate(operations, VoidRenderingContext()).export(emptyList(), NoContextBaseTabulationHandler(stream), this)
}

fun <T> TableBuilder<T>.export(file: File) {
    val ext = file.extension
    if (ext.isNotBlank()) {
        FileOutputStream(file).use {
            TableExportTemplate<T, OutputStream, WritableRenderingContext<OutputStream>>(TabulationFormat(file.extension)).export(
                emptyList(),
                BaseTabulationHandler(it),
                this
            )
        }
    } else error("Cannot resolve tabulation format")
}