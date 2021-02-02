package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareTableOperations
import java.io.OutputStream

interface InitOperation {
    fun initialize()
}

interface CreateTableOperation<T> {
    fun createTable(table: Table<T>): Table<T>
}

interface FinishOperation {
    fun finish(stream: OutputStream)
}

interface LifecycleOperations : InitOperation, FinishOperation

interface ColumnOperation<T> {
    fun renderColumn(context: AttributedColumn)
}

interface RowOperation<T> {
    fun renderRow(context: AttributedRow<T>)
}

interface RowCellOperation<T> {
    fun renderRowCell(context: AttributedCell)
}

interface TableOperations<T> : CreateTableOperation<T>, ColumnOperation<T>, RowOperation<T>, RowCellOperation<T>

class ExportOperations<T>(
    val lifecycleOperations: LifecycleOperations,
    val tableOperations: TableOperations<T>
)

interface AttributeOperationsFactory<T> {
    fun createTableAttributeOperations(): Set<TableAttributeOperation<out TableAttribute>>?
    fun createRowAttributeOperations(): Set<RowAttributeOperation<T, out RowAttribute>>?
    fun createColumnAttributeOperations(): Set<ColumnAttributeOperation<T, out ColumnAttribute>>?
    fun createCellAttributeOperations(): Set<CellAttributeOperation<T, out CellAttribute>>?
}

interface ExportOperationsFactory<T> {
    fun createLifecycleOperations(): LifecycleOperations
    fun createTableOperations(): TableOperations<T>
}

abstract class AdaptingLifecycleOperations<A>(val adaptee: A) : LifecycleOperations

abstract class AdaptingTableOperations<T, A>(val adaptee: A) : TableOperations<T>

abstract class ExportOperationConfiguringFactory<T>: ExportOperationsFactory<T> {

    abstract fun getExportOperationsFactory() : ExportOperationsFactory<T>

    abstract fun getAttributeOperationsFactory() : AttributeOperationsFactory<T>?

    override fun createLifecycleOperations(): LifecycleOperations {
        return getExportOperationsFactory().createLifecycleOperations()
    }

    override fun createTableOperations(): TableOperations<T> {
        val attributeOperationsFactory = getAttributeOperationsFactory()
        return if (attributeOperationsFactory != null) {
            AttributeAwareTableOperations(
                attributeOperationsFactory.createTableAttributeOperations(),
                attributeOperationsFactory.createColumnAttributeOperations(),
                attributeOperationsFactory.createRowAttributeOperations(),
                attributeOperationsFactory.createCellAttributeOperations(),
                getExportOperationsFactory().createTableOperations()
            )
        } else {
            return getExportOperationsFactory().createTableOperations()
        }
    }
}
