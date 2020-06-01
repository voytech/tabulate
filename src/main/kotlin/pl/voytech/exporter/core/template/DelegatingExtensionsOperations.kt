package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class DelegatingTableExtensionsOperations<A>(
    private val extensionOperations: List<TableExtensionOperation<out TableExtension, A>>?
) : TableExtensionsOperation<A> {

    private fun operationByClass(hint: KClass<out TableExtension>): TableExtensionOperation<TableExtension, A>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as TableExtensionOperation<TableExtension, A>
    }

    override fun applyTableExtensions(state: DelegateAPI<A>, extensions: Set<TableExtension>) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingRowExtensionsOperations<A>(
    private val extensionOperations: List<RowExtensionOperation<out RowExtension, A>>?
) : RowExtensionsOperation<A> {

    private fun operationByClass(hint: KClass<out RowExtension>): RowExtensionOperation<RowExtension, A>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as RowExtensionOperation<RowExtension, A>
    }

    override fun applyRowExtensions(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<RowExtension>) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingColumnExtensionsOperations<A>(
    private val extensionOperations: List<ColumnExtensionOperation<out ColumnExtension, A>>?
) : ColumnExtensionsOperation<A> {

    private fun operationByClass(hint: KClass<out ColumnExtension>): ColumnExtensionOperation<ColumnExtension, A>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as ColumnExtensionOperation<ColumnExtension, A>
    }

    override fun applyColumnExtensions(
        state: DelegateAPI<A>,
        coordinates: Coordinates,
        extensions: Set<ColumnExtension>
    ) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingCellExtensionsOperations<A>(
    private val extensionOperations: List<CellExtensionOperation<out CellExtension, A>>?
) : CellExtensionsOperation<A> {

    private fun operationByClass(hint: KClass<out CellExtension>): CellExtensionOperation<CellExtension, A>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as CellExtensionOperation<CellExtension, A>
    }

    override fun applyCellExtensions(state: DelegateAPI<A>, coordinates: Coordinates, extensions: Set<CellExtension>) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}