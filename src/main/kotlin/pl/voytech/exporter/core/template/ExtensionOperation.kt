package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.extension.*
import kotlin.reflect.KClass


interface ExtensionOperation<out T : Extension> {
    fun extensionType(): KClass<out T>
}

interface TableExtensionOperation<T: TableExtension> : ExtensionOperation<T> {
    fun apply(state: DelegateAPI, extension: T)
}

interface RowExtensionOperation<T: RowExtension> : ExtensionOperation<T> {
    fun apply(state: DelegateAPI, coordinates: Coordinates, extension: T)
}

interface CellExtensionOperation<T: CellExtension> : ExtensionOperation<T> {
    fun apply(state: DelegateAPI, coordinates: Coordinates, extension: T)
}

interface ColumnExtensionOperation<T: ColumnExtension> : ExtensionOperation<T> {
    fun apply(state: DelegateAPI, coordinates: Coordinates, extension: T)
}
