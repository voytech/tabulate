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

    private val delegatingExtensionOperations: DelegatingTableExtensionsOperations<A, T> =
        DelegatingTableExtensionsOperations(extensionOperations)

    override fun createTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A> {
        return initializeTable(state, table).also {
            table.tableExtensions?.let { delegatingExtensionOperations.applyTableExtensions(state,table, it) }
        }
    }

    abstract fun initializeTable(state: DelegateAPI<A>, table: Table<T>): DelegateAPI<A>
}

abstract class RowOperationWithExtensions<A>(
    extensionOperations: List<RowExtensionOperation<out RowExtension, A>>?
) : RowOperation<A> {

    private val delegatingExtensionOperations: DelegatingRowExtensionsOperations<A> =
        DelegatingRowExtensionsOperations(extensionOperations)

    override fun renderRow(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<RowExtension>?) {
        renderRow(state, coordinates).also {
            extensions?.let { delegatingExtensionOperations.applyRowExtensions(state, coordinates, it) }
        }
    }

    abstract fun renderRow(state: DelegateAPI<A>, coordinates: Coordinates)

}

class ColumnOperationsWithExtensions<A>(
    extensionOperations: List<ColumnExtensionOperation<out ColumnExtension, A>>?
) : ColumnOperation<A> {

    private val delegatingExtensionOperations: DelegatingColumnExtensionsOperations<A> =
        DelegatingColumnExtensionsOperations(extensionOperations)

    override fun renderColumn(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<ColumnExtension>?) {
        extensions?.let { delegatingExtensionOperations.applyColumnExtensions(state, coordinates, extensions) }
    }
}

abstract class HeaderCellOperationsWithExtensions<A>(
    extensionOperations: List<CellExtensionOperation<out CellExtension, A>>?
) : HeaderCellOperation<A> {

    private val delegatingExtensionOperations: DelegatingCellExtensionsOperations<A> =
        DelegatingCellExtensionsOperations(extensionOperations)

    override fun renderHeaderCell(
        state: DelegateAPI<A>,
        coordinates: Coordinates,
        columnTitle: Description?,
        extensions: Set<CellExtension>?
    ) {
        renderHeaderCell(state, coordinates, columnTitle?.title).also {
            extensions?.let { delegatingExtensionOperations.applyCellExtensions(state, coordinates, it) }
        }
    }

    abstract fun renderHeaderCell(state: DelegateAPI<A>, coordinates: Coordinates, columnTitle: String?)

}

abstract class RowCellOperationsWithExtensions<A>(
    extensionOperations: List<CellExtensionOperation<out CellExtension, A>>?
) : RowCellOperation<A> {

    private val delegatingExtensionOperations: DelegatingCellExtensionsOperations<A> =
        DelegatingCellExtensionsOperations(extensionOperations)

    override fun renderRowCell(
        state: DelegateAPI<A>,
        coordinates: Coordinates,
        value: CellValue?,
        extensions: Set<CellExtension>?
    ) {
        renderRowCell(state, coordinates, value).also {
            extensions?.let { delegatingExtensionOperations.applyCellExtensions(state, coordinates, it) }
        }
    }

    abstract fun renderRowCell(state: DelegateAPI<A>, coordinates: Coordinates, value: CellValue?)

}