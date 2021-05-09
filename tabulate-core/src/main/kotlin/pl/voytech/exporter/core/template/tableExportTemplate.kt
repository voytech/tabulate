package pl.voytech.exporter.core.template

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.api.builder.dsl.TableBuilderApi
import pl.voytech.exporter.core.api.builder.dsl.table
import pl.voytech.exporter.core.model.Column
import pl.voytech.exporter.core.model.NextId
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.ColumnRenderPhase
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes
import pl.voytech.exporter.core.template.iterators.OperationContextIterator
import pl.voytech.exporter.core.template.operations.ExportOperations
import pl.voytech.exporter.core.template.resolvers.BufferingRowContextResolver
import pl.voytech.exporter.core.template.source.CollectionSource
import pl.voytech.exporter.core.template.source.EmptySource
import pl.voytech.exporter.core.template.spi.ExportOperationsProvider
import pl.voytech.exporter.core.template.spi.Identifiable
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
        ops.tableRenderOperations.renderRow(rowContext).also {
            renderRowCells(stateAndAttributes, rowContext)
        }
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