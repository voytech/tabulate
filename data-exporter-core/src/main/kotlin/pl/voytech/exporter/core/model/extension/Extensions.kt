package pl.voytech.exporter.core.model.extension

open class Extension

abstract class CellExtension : Extension() {
    abstract fun mergeWith(other: CellExtension): CellExtension
}

open class ColumnExtension : Extension() {
    open fun beforeFirstRow(): Boolean = true
    open fun afterLastRow(): Boolean = false
}

open class RowExtension : Extension()

open class TableExtension : Extension()
