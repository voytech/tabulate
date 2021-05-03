package pl.voytech.exporter.core.template.operations

import org.reactivestreams.Publisher
import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.ResultHandler
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareTableOperations
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareTableRenderOperations
import pl.voytech.exporter.core.template.operations.impl.AttributesOperations


interface LifecycleOperations<T, O> {
    fun initialize(source : Publisher<T>, resultHandler: ResultHandler<T, O>)
    fun finish()
}

interface TableOperation<T> {
    fun createTable(builder: TableBuilder<T>): Table<T> = builder.build()
}

interface TableRenderOperations<T> {
    fun renderColumn(context: AttributedColumn) {}
    fun renderRow(context: AttributedRow<T>) {}
    fun renderRowCell(context: AttributedCell)
}

class ExportOperations<T, O>(
    val lifecycleOperations: LifecycleOperations<T, O>,
    val tableOperation: TableOperation<T>,
    val tableRenderOperations: TableRenderOperations<T>
)

interface ExportOperationsFactory<T, O> {
    fun createLifecycleOperations(): LifecycleOperations<T, O>
    fun createTableOperation(): TableOperation<T>
    fun createTableRenderOperations(): TableRenderOperations<T>
}

abstract class AdaptingLifecycleOperations<T, O, A>(val adaptee: A) : LifecycleOperations<T , O>

abstract class AdaptingTableRenderOperations<T, A>(val adaptee: A) : TableRenderOperations<T>

abstract class ExportOperationsConfiguringFactory<T, O> : ExportOperationsFactory<T, O> {

    private var attributeOperations: AttributesOperations<T>? = null

    abstract fun getExportOperationsFactory(): ExportOperationsFactory<T, O>

    abstract fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<T>?

    private fun prepareAttributeOperations(): AttributesOperations<T>? {
        return attributeOperations ?: getAttributeOperationsFactory()?.let {
            AttributesOperations(
                it.createTableAttributeRenderOperations(),
                it.createColumnAttributeRenderOperations(),
                it.createRowAttributeRenderOperations(),
                it.createCellAttributeRenderOperations()
            )
        }.also { attributeOperations = it }
    }

    override fun createLifecycleOperations(): LifecycleOperations<T, O> {
       return getExportOperationsFactory().createLifecycleOperations()
    }

    override fun createTableOperation(): TableOperation<T> {
        val tableOp = getExportOperationsFactory().createTableOperation()
        return prepareAttributeOperations().let {
            if (it != null) {
                AttributeAwareTableOperations(it, tableOp)
            } else {
                tableOp
            }
        }
    }

    override fun createTableRenderOperations(): TableRenderOperations<T> {
        val tableOperations = getExportOperationsFactory().createTableRenderOperations()
        return prepareAttributeOperations().let {
            if (it != null) {
                AttributeAwareTableRenderOperations(it, tableOperations)
            } else {
                tableOperations
            }
        }
    }

    fun createOperations(): ExportOperations<T, O> = ExportOperations(
        lifecycleOperations = createLifecycleOperations(),
        tableOperation = createTableOperation(),
        tableRenderOperations = createTableRenderOperations()
    )

}
