package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.model.Column
import io.github.voytech.tabulate.model.NextId
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.ColumnRenderPhase
import io.github.voytech.tabulate.template.context.GlobalContextAndAttributes
import io.github.voytech.tabulate.template.iterators.OperationContextIterator
import io.github.voytech.tabulate.template.operations.ExportOperations
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

    private lateinit var ops: ExportOperations<T, O>

    private lateinit var resultHandler: ResultHandler<T, O>

    constructor(output: TabulationFormat<T, O>) : this() {
        this.ops = resolveExportOperationsFactory(output)!!.createOperations()
        this.resultHandler = output.resultHandler

    }

    constructor(delegate: ExportOperations<T, O>, resultHandler: ResultHandler<T, O>) : this() {
        this.ops = delegate
        this.resultHandler = resultHandler
    }

    @Suppress("ReactiveStreamsSubscriberImplementation")
    inner class UnboundSubscriber(
        private val resolver: BufferingRowContextResolver<T>,
        private val iterator: OperationContextIterator<T, AttributedRow<T>>,
        private val context: GlobalContextAndAttributes<T>,
    ) : Subscriber<T> {

        override fun onSubscribe(subscription: Subscription) {
            renderColumns(context, ColumnRenderPhase.BEFORE_FIRST_ROW)
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
            while (iterator.hasNext()) {
                renderNextRow()
            }
            renderColumns(context, ColumnRenderPhase.AFTER_LAST_ROW)
            ops.lifecycleOperations.finish()
        }

        private fun renderNextRow() {
            if (iterator.hasNext()) {
                renderNextRow(context, iterator.next())
            }
        }
    }

    private fun bind(tableBuilder: TableBuilder<T>, source: Publisher<T>) {
        ops.tableOperation.createTable(tableBuilder).let { table ->
            GlobalContextAndAttributes(
                tableModel = table,
                tableName = table.name ?: "table${NextId.nextId()}",
                firstRow = table.firstRow,
                firstColumn = table.firstColumn
            ).also {
                val contextResolver = BufferingRowContextResolver(table, it)
                val contextIterator = OperationContextIterator(contextResolver)
                source.subscribe(UnboundSubscriber(contextResolver, contextIterator, it))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveExportOperationsFactory(id: Identifiable): ExportOperationsProvider<T, O>? {
        val loader: ServiceLoader<ExportOperationsProvider<*, *>> =
            ServiceLoader.load(ExportOperationsProvider::class.java)
        return loader.find { it.test(id) } as ExportOperationsProvider<T, O>?
    }

    fun export(tableBuilder: TableBuilder<T>, source: Publisher<T>) {
        ops.lifecycleOperations.initialize(source, resultHandler)
        bind(tableBuilder, source)
    }

    private fun renderColumns(stateAndAttributes: GlobalContextAndAttributes<T>, renderPhase: ColumnRenderPhase) {
        stateAndAttributes.tableModel.forEachColumn { columnIndex: Int, column: Column<T> ->
            ops.tableRenderOperations.renderColumn(
                stateAndAttributes.createColumnContext(IndexedValue(column.index ?: columnIndex, column), renderPhase)
            )
        }
    }

    private fun renderRowCells(stateAndAttributes: GlobalContextAndAttributes<T>, context: AttributedRow<T>) {
        stateAndAttributes.tableModel.forEachColumn { column: Column<T> ->
            if (context.rowCellValues.containsKey(column.id)) {
                ops.tableRenderOperations.renderRowCell(context.rowCellValues[column.id]!!)
            }
        }
    }

    private fun renderNextRow(
        stateAndAttributes: GlobalContextAndAttributes<T>,
        rowContext: AttributedRow<T>
    ) {
        ops.tableRenderOperations.beginRow(rowContext)
        renderRowCells(stateAndAttributes, rowContext)
        ops.tableRenderOperations.endRow(rowContext)
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
    TableExportTemplate(TabulationFormat(file.extension) { _: Publisher<T> -> FileOutputStream(file) }).export(table(block),
        CollectionSource(this))
}

fun <T, O> Publisher<T>.tabulate(
    operations: ExportOperations<T, O>,
    resultHandler: ResultHandler<T, O>,
    block: TableBuilderApi<T>.() -> Unit,
) {
    TableExportTemplate(operations, resultHandler).export(table(block), this)
}

fun <T, O> Collection<T>.tabulate(
    operations: ExportOperations<T, O>,
    resultHandler: ResultHandler<T, O>,
    block: TableBuilderApi<T>.() -> Unit,
) {
    TableExportTemplate(operations, resultHandler).export(table(block), CollectionSource(this))
}

fun <T> TableBuilder<T>.export(operations: ExportOperations<T, OutputStream>, stream: OutputStream) {
    TableExportTemplate(operations) { stream }.export(this, EmptySource())
}

fun <T> TableBuilder<T>.export(file: File) {
    TableExportTemplate(TabulationFormat(file.extension) { _: Publisher<T> -> FileOutputStream(file) }).export(this,
        EmptySource())
}