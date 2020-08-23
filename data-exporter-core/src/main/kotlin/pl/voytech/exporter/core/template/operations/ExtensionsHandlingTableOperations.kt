package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension
import pl.voytech.exporter.core.template.*

abstract class ExtensionsHandlingTableOperations<T, A>(
    tableExtensionsOperations: List<TableExtensionOperation<out TableExtension, A>>?,
    rowExtensionOperations: List<RowExtensionOperation<T, out RowExtension, A>>?,
    columnExtensionOperations: List<ColumnExtensionOperation<T, out ColumnExtension, A>>?,
    cellExtensionOperations: List<CellExtensionOperation<T, out CellExtension, A>>?
) : TableOperations<T, A> {

    private val delegates: DelegatingTableExtensionsOperations<T, A> =
        DelegatingTableExtensionsOperations(
            tableExtensionsOperations, columnExtensionOperations, rowExtensionOperations, cellExtensionOperations
        )

    override fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A> {
        return initializeTable(state, table).also {
            table.tableExtensions?.let { delegates.applyTableExtensions(state, table, it) }
        }
    }

    abstract fun initializeTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>

    override fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        extensions: Set<RowExtension>?
    ) {
        renderRow(state, context).also {
            extensions?.let { delegates.applyRowExtensions(state, context, it) }
        }
    }

    abstract fun renderRow(state: DelegateAPI<A>, context: OperationContext<T, RowOperationTableData<T>>)

    override fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        extensions: Set<ColumnExtension>?
    ) {
        extensions?.let { delegates.applyColumnExtensions(state, context, extensions) }
    }

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        extensions: Set<CellExtension>?
    ) {
        renderRowCell(state, context).also {
            extensions?.let { delegates.applyCellExtensions(state, context, it) }
        }
    }

    abstract fun renderRowCell(state: DelegateAPI<A>, context: OperationContext<T, CellOperationTableData<T>>)

}

