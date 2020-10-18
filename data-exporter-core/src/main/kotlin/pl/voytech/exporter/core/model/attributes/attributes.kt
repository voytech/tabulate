package pl.voytech.exporter.core.model.attributes

open class Attribute

abstract class CellAttribute : Attribute() {
    abstract fun mergeWith(other: CellAttribute): CellAttribute
}

open class ColumnAttribute : Attribute() {
    open fun beforeFirstRow(): Boolean = true
    open fun afterLastRow(): Boolean = false
}

open class RowAttribute : Attribute()

open class TableAttribute : Attribute()
