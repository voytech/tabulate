package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class DelegatingTableExtensionsOperations<T, A>(
    private val extensionOperations: List<TableExtensionOperation<out TableExtension, A>>?
) : TableExtensionsOperation<T, A> {

    private fun operationByClass(hint: KClass<out TableExtension>): TableExtensionOperation<TableExtension, A>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as TableExtensionOperation<TableExtension, A>
    }

    override fun applyTableExtensions(state: DelegateAPI<A>, table: Table<T>, extensions: Set<TableExtension>) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, table, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingRowExtensionsOperations<T, A>(
    private val extensionOperations: List<RowExtensionOperation<T, out RowExtension, A>>?
) : RowExtensionsOperation<T, A> {

    private fun operationByClass(hint: KClass<out RowExtension>): RowExtensionOperation<T, RowExtension, A>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as RowExtensionOperation<T, RowExtension, A>
    }

    override fun applyRowExtensions(state: DelegateAPI<A>, context: OperationContext<T,RowOperationTableDataContext<T>>, extensions: Set<RowExtension>) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, context, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingColumnExtensionsOperations<T, A>(
    private val extensionOperations: List<ColumnExtensionOperation<T, out ColumnExtension, A>>?
) : ColumnExtensionsOperation<T, A> {

    private fun operationByClass(hint: KClass<out ColumnExtension>): ColumnExtensionOperation<T, ColumnExtension, A>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as ColumnExtensionOperation<T, ColumnExtension, A>
    }

    override fun applyColumnExtensions(
        state: DelegateAPI<A>,
        context: OperationContext<T,ColumnOperationTableDataContext<T>>,
        extensions: Set<ColumnExtension>
    ) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, context, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingCellExtensionsOperations<T, A>(
    private val extensionOperations: List<CellExtensionOperation<T, out CellExtension, A>>?
) : CellExtensionsOperation<T, A> {

    private fun operationByClass(hint: KClass<out CellExtension>): CellExtensionOperation<T, CellExtension, A>? {
        return extensionOperations?.find { operation -> operation.extensionType() == hint } as CellExtensionOperation<T, CellExtension, A>
    }

    override fun applyCellExtensions(state: DelegateAPI<A>, context: OperationContext<T,CellOperationTableDataContext<T>>, extensions: Set<CellExtension>) {
        extensions.forEach { hint ->
            operationByClass(hint::class)?.apply(state, context, hint)
        }
    }
}