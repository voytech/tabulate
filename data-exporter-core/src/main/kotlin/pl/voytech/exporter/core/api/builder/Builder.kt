package pl.voytech.exporter.core.api.builder

import pl.voytech.exporter.core.model.extension.*

@TableMarker
interface Builder<T> {
    fun build(): T
}

interface ExtensionBuilder<T : Extension> : Builder<T>

interface CellExtensionBuilder : ExtensionBuilder<CellExtension>

interface RowExtensionBuilder : ExtensionBuilder<RowExtension>

interface ColumnExtensionBuilder : ExtensionBuilder<ColumnExtension>

interface TableExtensionBuilder : ExtensionBuilder<TableExtension>

abstract class InternalBuilder<T> {
    internal abstract fun build(): T
}