package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension

abstract class RowOperationWithExtensions(
    extensionOperations: List<RowExtensionOperation<out RowExtension>>?
): RowOperation {

    private val delegatingExtensionOperations: DelegatingRowExtensionsOperations = DelegatingRowExtensionsOperations(extensionOperations)

    override fun renderRow(state: DelegateAPI, coordinates: Coordinates, extensions: Set<RowExtension>?) {
        renderRow(state, coordinates).also {
            extensions?.let { delegatingExtensionOperations.applyRowExtensions(state, coordinates, it) }
        }
    }

    abstract fun renderRow(state: DelegateAPI, coordinates: Coordinates)

}

class ColumnOperationsWithExtensions(
    extensionOperations: List<ColumnExtensionOperation<out ColumnExtension>>?
): ColumnOperation {

    private val delegatingExtensionOperations: DelegatingColumnExtensionsOperations = DelegatingColumnExtensionsOperations(extensionOperations)

    override fun renderColumn(state: DelegateAPI, coordinates: Coordinates, extensions: Set<ColumnExtension>?) {
        extensions?.let { delegatingExtensionOperations.applyColumnExtensions(state, coordinates, extensions) }
    }
}

abstract class HeaderCellOperationsWithExtensions(
    extensionOperations: List<CellExtensionOperation<out CellExtension>>?
): HeaderCellOperation {

    private val delegatingExtensionOperations: DelegatingCellExtensionsOperations = DelegatingCellExtensionsOperations(extensionOperations)

    override fun renderHeaderCell(
        state: DelegateAPI,
        coordinates: Coordinates,
        columnTitle: Description?,
        extensions: Set<CellExtension>?
    ) {
        renderHeaderCell(state, coordinates, columnTitle?.title).also {
            extensions?.let { delegatingExtensionOperations.applyCellExtensions(state, coordinates, it) }
        }
    }

    abstract fun renderHeaderCell(state: DelegateAPI, coordinates: Coordinates, columnTitle: String?)

}

abstract class RowCellOperationsWithExtensions(
    extensionOperations: List<CellExtensionOperation<out CellExtension>>?
): RowCellOperation {

    private val delegatingExtensionOperations: DelegatingCellExtensionsOperations = DelegatingCellExtensionsOperations(extensionOperations)

    override fun renderRowCell(state: DelegateAPI, coordinates: Coordinates, value: CellValue?, extensions: Set<CellExtension>?) {
        renderRowCell(state, coordinates, value).also {
            extensions?.let { delegatingExtensionOperations.applyCellExtensions(state, coordinates, it) }
        }
    }

    abstract fun renderRowCell(state: DelegateAPI, coordinates: Coordinates, value: CellValue?)

}