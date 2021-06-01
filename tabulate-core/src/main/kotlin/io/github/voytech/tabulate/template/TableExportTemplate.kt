package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.model.ColumnDef
import io.github.voytech.tabulate.model.NextId
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.ColumnRenderPhase
import io.github.voytech.tabulate.template.context.IndexLabel
import io.github.voytech.tabulate.template.context.TableExportingState
import io.github.voytech.tabulate.template.iterators.OperationContextIterator
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver
import io.github.voytech.tabulate.template.source.CollectionSource
import io.github.voytech.tabulate.template.source.EmptySource
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.template.spi.Identifiable
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

/**
 * Tabulate frontend facade. Role of this class is to orchestrate and bind all things together. Its like an entry point.
 * @author Wojciech Mąka
 */
open class TableExportTemplate<T, O>() {

    private lateinit var ops: TableExportOperations<T, O>

    private lateinit var resultHandler: ResultHandler<T, O>

    constructor(output: TabulationFormat<T, O>) : this() {
        this.ops = resolveExportOperationsFactory(output)!!.create()
        this.resultHandler = output.resultHandler

    }

    constructor(delegate: TableExportOperations<T, O>, resultHandler: ResultHandler<T, O>) : this() {
        this.ops = delegate
        this.resultHandler = resultHandler
    }

    @Suppress("ReactiveStreamsSubscriberImplementation")
    inner class UnboundSubscriber(
        private val state: TableExportingState<T>,
        private val resolver: BufferingRowContextResolver<T> = BufferingRowContextResolver(state.tableModel),
        private var iterator: OperationContextIterator<T, AttributedRow<T>> = OperationContextIterator(resolver),
    ) : Subscriber<T> {

        init {
            //TODO remove this receiver interface and pass directly via constructor on resolver and iterator.
            iterator.setState(state)
        }

        override fun onSubscribe(subscription: Subscription) {
            renderColumns(state, ColumnRenderPhase.BEFORE_FIRST_ROW)
            subscription.request(UNBOUND)

        }

        override fun onNext(record: T) {
            resolver.buffer(record)
            renderNextRow()
        }

        override fun onError(t: Throwable?) {
            TODO("Not yet implemented - renderOnError ?")
        }

        override fun onComplete() {
            renderBufferedRows()
            renderRowsAfterDataProcessed()
            renderColumns(state, ColumnRenderPhase.AFTER_LAST_ROW)
            ops.finish()
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

    private fun bind(tableBuilder: TableBuilder<T>, source: Publisher<T>) {
        ops.createTable(tableBuilder).let { table ->
            TableExportingState(
                tableModel = table,
                tableName = table.name ?: "table${NextId.nextId()}",
                firstRow = table.firstRow,
                firstColumn = table.firstColumn
            ).also { source.subscribe(UnboundSubscriber(it)) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveExportOperationsFactory(id: Identifiable): ExportOperationsProvider<T, O>? {
        val loader: ServiceLoader<ExportOperationsProvider<*, *>> =
            ServiceLoader.load(ExportOperationsProvider::class.java)
        return loader.find { it.test(id) } as ExportOperationsProvider<T, O>?
    }

    fun export(tableBuilder: TableBuilder<T>, source: Publisher<T>) {
        ops.initialize(source, resultHandler)
        bind(tableBuilder, source)
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

    private fun renderNextRow(
        tableExportingState: TableExportingState<T>,
        rowContext: AttributedRow<T>,
    ) {
        ops.beginRow(rowContext)
        renderRowCells(tableExportingState, rowContext)
        ops.endRow(rowContext)
    }

    companion object {
        private const val UNBOUND = Long.MAX_VALUE
    }
}

fun <T, O> Publisher<T>.tabulate(format: TabulationFormat<T, O>, block: TableBuilderApi<T>.() -> Unit) {
    TableExportTemplate(format).export(table(block), this)
}

fun <T, O> Collection<T>.tabulate(format: TabulationFormat<T, O>, block: TableBuilderApi<T>.() -> Unit) {
    TableExportTemplate(format).export(table(block), CollectionSource(this))
}

fun <T> Collection<T>.tabulate(fileName: String, block: TableBuilderApi<T>.() -> Unit) {
    val file = File(fileName)
    TableExportTemplate(TabulationFormat(file.extension) { _: Publisher<T> -> FileOutputStream(file) }).export(table(
        block),
        CollectionSource(this))
}

fun <T, O> Publisher<T>.tabulate(
    operations: TableExportOperations<T, O>,
    resultHandler: ResultHandler<T, O>,
    block: TableBuilderApi<T>.() -> Unit,
) {
    TableExportTemplate(operations, resultHandler).export(table(block), this)
}

fun <T, O> Collection<T>.tabulate(
    operations: TableExportOperations<T, O>,
    resultHandler: ResultHandler<T, O>,
    block: TableBuilderApi<T>.() -> Unit,
) {
    TableExportTemplate(operations, resultHandler).export(table(block), CollectionSource(this))
}

fun <T> TableBuilder<T>.export(operations: TableExportOperations<T, OutputStream>, stream: OutputStream) {
    TableExportTemplate(operations) { stream }.export(this, EmptySource())
}

fun <T> TableBuilder<T>.export(file: File) {
    TableExportTemplate(TabulationFormat(file.extension) { _: Publisher<T> -> FileOutputStream(file) }).export(this,
        EmptySource())
}