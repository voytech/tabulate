package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension

abstract class CreateTableOperationWithExtensions<T, A>(
    extensionOperations: List<TableExtensionOperation<out TableExtension, A>>?
) : CreateTableOperation<T, A> {

    private val delegatingExtensionOperations: DelegatingTableExtensionsOperations<T, A> =
        DelegatingTableExtensionsOperations(extensionOperations)

    override fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A> {
        return initializeTable(state, table).also {
            table.tableExtensions?.let { delegatingExtensionOperations.applyTableExtensions(state,table, it) }
        }
    }

    abstract fun initializeTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>
}

abstract class RowOperationWithExtensions<T, A>(
    extensionOperations: List<RowExtensionOperation<T, out RowExtension, A>>?
) : RowOperation<T, A> {

    private val delegatingExtensionOperations: DelegatingRowExtensionsOperations<T, A> =
        DelegatingRowExtensionsOperations(extensionOperations)

    override fun renderRow(state: DelegateAPI<A>, context: OperationContext<T,RowOperationTableDataContext<T>>, extensions: Set<RowExtension>?) {
        renderRow(state, context).also {
            extensions?.let { delegatingExtensionOperations.applyRowExtensions(state, context, it) }
        }
    }

    abstract fun renderRow(state: DelegateAPI<A>, context: OperationContext<T,RowOperationTableDataContext<T>>)

}

class ColumnOperationsWithExtensions<T, A>(
    extensionOperations: List<ColumnExtensionOperation<T,out ColumnExtension, A>>?
) : ColumnOperation<T, A> {

    private val delegatingExtensionOperations: DelegatingColumnExtensionsOperations<T, A> =
        DelegatingColumnExtensionsOperations(extensionOperations)

    override fun renderColumn(state: DelegateAPI<A>, context: OperationContext<T, ColumnOperationTableDataContext<T>>, extensions: Set<ColumnExtension>?) {
        extensions?.let { delegatingExtensionOperations.applyColumnExtensions(state, context, extensions) }
    }
}

abstract class HeaderCellOperationsWithExtensions<T, A>(
    extensionOperations: List<CellExtensionOperation<T, out CellExtension, A>>?
) : HeaderCellOperation<T, A> {

    private val delegatingExtensionOperations: DelegatingCellExtensionsOperations<T, A> =
        DelegatingCellExtensionsOperations(extensionOperations)

    override fun renderHeaderCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableDataContext<T>>,
        columnTitle: Description?,
        extensions: Set<CellExtension>?
    ) {
        renderHeaderCell(state, context, columnTitle?.title).also {
            extensions?.let { delegatingExtensionOperations.applyCellExtensions(state, context, it) }
        }
    }

    abstract fun renderHeaderCell(state: DelegateAPI<A>, context: OperationContext<T, CellOperationTableDataContext<T>>, columnTitle: String?)

}

abstract class RowCellOperationsWithExtensions<T, A>(
    extensionOperations: List<CellExtensionOperation<T, out CellExtension, A>>?
) : RowCellOperation<T, A> {

    private val delegatingExtensionOperations: DelegatingCellExtensionsOperations<T,A> =
        DelegatingCellExtensionsOperations(extensionOperations)

    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableDataContext<T>>,
        value: CellValue?,
        extensions: Set<CellExtension>?
    ) {
        renderRowCell(state, context, value).also {
            extensions?.let { delegatingExtensionOperations.applyCellExtensions(state, context, it) }
        }
    }

    abstract fun renderRowCell(state: DelegateAPI<A>, context: OperationContext<T, CellOperationTableDataContext<T>>, value: CellValue?)

}