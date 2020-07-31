package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.*
import kotlin.reflect.KClass


interface ExtensionOperation<out T : Extension> {
    fun extensionType(): KClass<out T>
}

interface TableExtensionOperation<T : TableExtension, A> : ExtensionOperation<T> {
    fun renderExtension(state: DelegateAPI<A>, table: Table<*>, extension: T)
}

interface RowExtensionOperation<E,T : RowExtension, A> : ExtensionOperation<T> {
    fun renderExtension(state: DelegateAPI<A>, context: OperationContext<E,RowOperationTableData<E>>, extension: T)
}

interface CellExtensionOperation<E, T : CellExtension, A> : ExtensionOperation<T> {
    fun renderExtension(state: DelegateAPI<A>, context: OperationContext<E,CellOperationTableData<E>>, extension: T)
}

interface ColumnExtensionOperation<E, T : ColumnExtension, A> : ExtensionOperation<T> {
    fun renderExtension(state: DelegateAPI<A>, context: OperationContext<E,ColumnOperationTableData<E>>, extension: T)
}
