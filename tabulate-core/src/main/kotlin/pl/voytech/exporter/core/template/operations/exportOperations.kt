package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareLifecycleOperations
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareTableRenderOperations
import pl.voytech.exporter.core.template.operations.impl.AttributesOperations
import java.io.OutputStream

interface InitOperation {
    fun initialize() {}
}

interface CreateTableOperation<T> {
    fun createTable(builder: TableBuilder<T>): Table<T> = builder.build()
}

interface FinishOperation {
    fun finish(stream: OutputStream)
}

interface LifecycleOperations<T> : InitOperation, CreateTableOperation<T>, FinishOperation

interface ColumnRenderOperation<T> {
    fun renderColumn(context: AttributedColumn) {}
}

interface RowRenderOperation<T> {
    fun renderRow(context: AttributedRow<T>) {}
}

interface CellRenderOperation<T> {
    fun renderRowCell(context: AttributedCell)
}

interface TableRenderOperations<T> : ColumnRenderOperation<T>, RowRenderOperation<T>, CellRenderOperation<T>

class ExportOperations<T>(
    val lifecycleOperations: LifecycleOperations<T>,
    val tableRenderOperations: TableRenderOperations<T>
)

interface ExportOperationsFactory<T> {
    fun createLifecycleOperations(): LifecycleOperations<T>
    fun createTableRenderOperations(): TableRenderOperations<T>
}

abstract class AdaptingLifecycleOperations<T, A>(val adaptee: A) : LifecycleOperations<T>

abstract class AdaptingTableRenderOperations<T, A>(val adaptee: A) : TableRenderOperations<T>

abstract class ExportOperationConfiguringFactory<T> : ExportOperationsFactory<T> {

    private var attributeOperations: AttributesOperations<T>? = null

    abstract fun getExportOperationsFactory(): ExportOperationsFactory<T>

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

    override fun createLifecycleOperations(): LifecycleOperations<T> {
        val lifecycleOperations = getExportOperationsFactory().createLifecycleOperations()
        return prepareAttributeOperations().let {
            if (it != null) {
                AttributeAwareLifecycleOperations(it, lifecycleOperations)
            } else {
                lifecycleOperations
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

    fun createOperations(): ExportOperations<T> = ExportOperations(
        lifecycleOperations = createLifecycleOperations(),
        tableRenderOperations = createTableRenderOperations()
    )

}
