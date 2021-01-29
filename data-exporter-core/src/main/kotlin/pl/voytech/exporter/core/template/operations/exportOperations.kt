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

abstract class AdaptingAttributeOperationsFactory<T, A>(open val adaptee: A) : AttributeOperationsFactory<T> {
    abstract override fun createTableAttributeOperations(): Set<AdaptingTableAttributeOperation<A, out TableAttribute>>?
    abstract override fun createRowAttributeOperations(): Set<AdaptingRowAttributeOperation<A, T, out RowAttribute>>?
    abstract override fun createColumnAttributeOperations(): Set<AdaptingColumnAttributeOperation<A, T, out ColumnAttribute>>?
    abstract override fun createCellAttributeOperations(): Set<AdaptingCellAttributeOperation<A, T, out CellAttribute>>?
}

abstract class AdaptingExportOperationsFactory<T, A>(override val adaptee: A) : ExportOperationsFactory<T>, AdaptingAttributeOperationsFactory<T, A>(adaptee) {
    abstract override fun createLifecycleOperations(): AdaptingLifecycleOperations<A>
    abstract override fun createTableOperations(): AdaptingTableOperations<T, A>
}

class AttributeAwareExportOperationsFactory<T, A>(
    private val exportOperationsFactory: AdaptingExportOperationsFactory<T, A>,
): ExportOperationsFactory<T> {

    override fun createLifecycleOperations(): AdaptingLifecycleOperations<A> {
        return exportOperationsFactory.createLifecycleOperations()
    }

    override fun createTableOperations(): TableOperations<T> {
        return AttributeAwareTableOperations(
            exportOperationsFactory.createTableAttributeOperations(),
            exportOperationsFactory.createColumnAttributeOperations(),
            exportOperationsFactory.createRowAttributeOperations(),
            exportOperationsFactory.createCellAttributeOperations(),
            exportOperationsFactory.createTableOperations()
        )
    }
}
