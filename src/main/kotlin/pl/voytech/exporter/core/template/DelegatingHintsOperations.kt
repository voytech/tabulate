package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.extension.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class DelegatingTableExtensionsOperations(
    private val extensionOperations: List<TableExtensionOperation<out TableExtension>>?
) : TableExtensionsOperation {

    private fun operationByClass(hint: KClass<out TableExtension>) : TableExtensionOperation<TableExtension>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as TableExtensionOperation<TableExtension>
    }

    override fun applyTableExtensions(state: DelegateAPI, extensions: Set<TableExtension>) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingRowExtensionsOperations(
    private val extensionOperations: List<RowExtensionOperation<out RowExtension>>?
) : RowExtensionsOperation {

    private fun operationByClass(hint: KClass<out RowExtension>) : RowExtensionOperation<RowExtension>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as RowExtensionOperation<RowExtension>
    }

    override fun applyRowExtensions(state: DelegateAPI, coordinates: Coordinates, extensions: Set<RowExtension>)  {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingColumnExtensionsOperations(
    private val extensionOperations: List<ColumnExtensionOperation<out ColumnExtension>>?
) : ColumnExtensionsOperation {

    private fun operationByClass(hint: KClass<out ColumnExtension>) : ColumnExtensionOperation<ColumnExtension>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as ColumnExtensionOperation<ColumnExtension>
    }

    override fun applyColumnExtensions(state: DelegateAPI, coordinates: Coordinates, extensions: Set<ColumnExtension>)  {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingCellExtensionsOperations(
    private val extensionOperations: List<CellExtensionOperation<out CellExtension>>?
) : CellExtensionsOperation {

    private fun operationByClass(hint: KClass<out CellExtension>) : CellExtensionOperation<CellExtension>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as CellExtensionOperation<CellExtension>
    }

    override fun applyCellExtensions(state: DelegateAPI, coordinates: Coordinates, extensions: Set<CellExtension>)  {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}