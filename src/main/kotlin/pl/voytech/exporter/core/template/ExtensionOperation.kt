package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.extension.*
import kotlin.reflect.KClass


interface ExtensionOperation<out T : Extension> {
    fun extensionType(): KClass<out T>
}

interface TableExtensionOperation<T: TableExtension,A> : ExtensionOperation<T> {
    fun apply(state: DelegateAPI<A>, extension: T)
}

interface RowExtensionOperation<T: RowExtension,A> : ExtensionOperation<T> {
    fun apply(state: DelegateAPI<A>, coordinates: Coordinates, extension: T)
}

interface CellExtensionOperation<T: CellExtension,A> : ExtensionOperation<T> {
    fun apply(state: DelegateAPI<A>, coordinates: Coordinates, extension: T)
}

interface ColumnExtensionOperation<T: ColumnExtension,A> : ExtensionOperation<T> {
    fun apply(state: DelegateAPI<A>, coordinates: Coordinates, extension: T)
}
