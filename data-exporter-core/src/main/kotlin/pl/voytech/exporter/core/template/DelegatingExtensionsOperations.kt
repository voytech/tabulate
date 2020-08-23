package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.ColumnExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class DelegatingTableExtensionsOperations<T, A>(
    private val tableExtensionOperations: List<TableExtensionOperation<out TableExtension, A>>?,
    private val columnExtensionOperations: List<ColumnExtensionOperation<T, out ColumnExtension, A>>?,
    private val rowExtensionOperations: List<RowExtensionOperation<T, out RowExtension, A>>?,
    private val cellExtensionOperations: List<CellExtensionOperation<T, out CellExtension, A>>?
) : TableExtensionsOperations<T, A> {

    private fun tableExtensionOperationByClass(hint: KClass<out TableExtension>): TableExtensionOperation<TableExtension, A>? {
        return tableExtensionOperations?.find { operation -> operation.extensionType() == hint } as TableExtensionOperation<TableExtension, A>
    }

    private fun rowExtensionOperationByClass(hint: KClass<out RowExtension>): RowExtensionOperation<T, RowExtension, A>? {
        return rowExtensionOperations?.find { operation -> operation.extensionType() == hint } as RowExtensionOperation<T, RowExtension, A>
    }

    private fun columnExtensionOperationByClass(hint: KClass<out ColumnExtension>): ColumnExtensionOperation<T, ColumnExtension, A>? {
        return columnExtensionOperations?.find { operation -> operation.extensionType() == hint } as ColumnExtensionOperation<T, ColumnExtension, A>
    }

    private fun cellExtensionOperationByClass(hint: KClass<out CellExtension>): CellExtensionOperation<T, CellExtension, A>? {
        return cellExtensionOperations?.find { operation -> operation.extensionType() == hint } as CellExtensionOperation<T, CellExtension, A>
    }


    override fun applyTableExtensions(state: DelegateAPI<A>, table: Table<T>, extensions: Set<TableExtension>) {
        extensions.forEach { hint ->
            tableExtensionOperationByClass(hint::class)?.renderExtension(state, table, hint)
        }
    }

    override fun applyColumnExtensions(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>,
        extensions: Set<ColumnExtension>
    ) {
        extensions.forEach { hint ->
            columnExtensionOperationByClass(hint::class)?.renderExtension(state, context, hint)
        }
    }

    override fun applyRowExtensions(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>,
        extensions: Set<RowExtension>
    ) {
        extensions.forEach { hint ->
            rowExtensionOperationByClass(hint::class)?.renderExtension(state, context, hint)
        }
    }

    override fun applyCellExtensions(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>,
        extensions: Set<CellExtension>
    ) {
        extensions.forEach { hint ->
            cellExtensionOperationByClass(hint::class)?.renderExtension(state, context, hint)
        }
    }
}