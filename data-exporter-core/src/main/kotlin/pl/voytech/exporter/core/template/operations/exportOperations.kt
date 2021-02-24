package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareTableRenderOperations
import java.io.OutputStream

interface InitOperation {
    fun initialize()
}

interface FinishOperation {
    fun finish(stream: OutputStream)
}

interface LifecycleOperations : InitOperation, FinishOperation

interface CreateTableOperation<T> {
    fun createTable(builder: TableBuilder<T>): Table<T> {
        return builder.build()
    }
}

interface ColumnRenderOperation<T> {
    fun renderColumn(context: AttributedColumn)
}

interface RowRenderOperation<T> {
    fun renderRow(context: AttributedRow<T>)
}

interface CellRenderOperation<T> {
    fun renderRowCell(context: AttributedCell)
}

interface TableRenderOperations<T> : CreateTableOperation<T>, ColumnRenderOperation<T>, RowRenderOperation<T>, CellRenderOperation<T>

class ExportOperations<T>(
    val lifecycleOperations: LifecycleOperations,
    val tableRenderOperations: TableRenderOperations<T>
)

interface ExportOperationsFactory<T> {
    fun createLifecycleOperations(): LifecycleOperations
    fun createTableRenderOperations(): TableRenderOperations<T>
}

abstract class AdaptingLifecycleOperations<A>(val adaptee: A) : LifecycleOperations

abstract class AdaptingTableRenderOperations<T, A>(val adaptee: A) : TableRenderOperations<T>

abstract class ExportOperationConfiguringFactory<T>: ExportOperationsFactory<T> {

    abstract fun getExportOperationsFactory() : ExportOperationsFactory<T>

    abstract fun getAttributeOperationsFactory() : AttributeRenderOperationsFactory<T>?

    override fun createLifecycleOperations(): LifecycleOperations {
        return getExportOperationsFactory().createLifecycleOperations()
    }

    override fun createTableRenderOperations(): TableRenderOperations<T> {
        val attributeOperationsFactory = getAttributeOperationsFactory()
        return if (attributeOperationsFactory != null) {
            AttributeAwareTableRenderOperations(
                attributeOperationsFactory.createTableAttributeRenderOperations(),
                attributeOperationsFactory.createColumnAttributeRenderOperations(),
                attributeOperationsFactory.createRowAttributeRenderOperations(),
                attributeOperationsFactory.createCellAttributeRenderOperations(),
                getExportOperationsFactory().createTableRenderOperations()
            )
        } else {
            return getExportOperationsFactory().createTableRenderOperations()
        }
    }
}
