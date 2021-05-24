package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.api.builder.*
import io.github.voytech.tabulate.model.*

import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.column.width

@DslMarker
annotation class TabulateMarker

@JvmSynthetic
fun <T> table(block: TableBuilderApi<T>.() -> Unit): TableBuilder<T> {
    return TableBuilder<T>().also {
        TableBuilderApi.new(it).apply(block)
    }
}

@TabulateMarker
class TableLevelAttributesBuilderApi<T> internal constructor(private val builder: TableBuilder<T>) {

    @JvmSynthetic
    fun attribute(attribute: Attribute<*>) = builder.attributes(attribute)

}

@TabulateMarker
class ColumnLevelAttributesBuilderApi<T> internal constructor(private val builder: ColumnBuilder<T>) {

    @JvmSynthetic
    fun attribute(attribute: ColumnAttribute) = builder.attributes(attribute)

    @JvmSynthetic
    fun attribute(attribute: CellAttribute) = builder.attributes(attribute)

}

@TabulateMarker
class RowLevelAttributesBuilderApi<T> internal constructor(private val builder: RowBuilder<T>) {
    @JvmSynthetic
    fun attribute(attribute: RowAttribute) = builder.attributes(attribute)

    @JvmSynthetic
    fun attribute(attribute: CellAttribute) = builder.attributes(attribute)
}

@TabulateMarker
class CellLevelAttributesBuilderApi<T> internal constructor(private val builder: CellBuilder<T>) {
    @JvmSynthetic
    fun attribute(attribute: CellAttribute) = builder.attributes(attribute)
}


@TabulateMarker
class TableBuilderApi<T> private constructor(private val builder: TableBuilder<T>)  {

    @set:JvmSynthetic
    var name: String? by this.builder::name

    @set:JvmSynthetic
    var firstRow: Int? by this.builder::firstRow

    @set:JvmSynthetic
    var firstColumn: Int? by this.builder::firstColumn

    @JvmSynthetic
    fun columns(block: ColumnsBuilderApi<T>.() -> Unit) = ColumnsBuilderApi(builder.columnsBuilder).apply(block)

    @JvmSynthetic
    fun rows(block: RowsBuilderApi<T>.() -> Unit) = RowsBuilderApi(builder.rowsBuilder).apply(block)

    @JvmSynthetic
    fun attributes(block: TableLevelAttributesBuilderApi<T>.() -> Unit) = TableLevelAttributesBuilderApi(builder).apply(block)

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: TableBuilder<T>): TableBuilderApi<T> = TableBuilderApi(builder)
    }

}

@TabulateMarker
class ColumnsBuilderApi<T> internal constructor(private val builder: ColumnsBuilder<T>)  {

    @set:JvmSynthetic
    var count: Int? by this.builder::count

    @JvmSynthetic
    fun column(id: String) = builder.addColumnBuilder(id) {}

    @JvmSynthetic
    fun column(id: String, block: ColumnBuilderApi<T>.() -> Unit) = builder.addColumnBuilder(id) {
        it.attributes(ColumnWidthAttribute(auto = true))
        ColumnBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?), block: ColumnBuilderApi<T>.() -> Unit) = builder.addColumnBuilder(ref) {
        it.attributes(ColumnWidthAttribute(auto = true))
        ColumnBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?)) = builder.addColumnBuilder(ref) {
        it.attributes(ColumnWidthAttribute(auto = true))
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: ColumnsBuilder<T>): ColumnsBuilderApi<T> = ColumnsBuilderApi(builder)
    }
}

@TabulateMarker
class ColumnBuilderApi<T> private constructor(private val builder: ColumnBuilder<T>)   {
    @set:JvmSynthetic
    var id: ColumnKey<T> by builder::id

    @set:JvmSynthetic
    var columnType: CellType? by builder::columnType

    @set:JvmSynthetic
    var index: Int?  by builder::index

    @JvmSynthetic
    fun attributes(block: ColumnLevelAttributesBuilderApi<T>.() -> Unit) = ColumnLevelAttributesBuilderApi(builder).apply(block)

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: ColumnBuilder<T>): ColumnBuilderApi<T> = ColumnBuilderApi(builder)
    }
}

@TabulateMarker
class RowsBuilderApi<T> internal constructor(private val builder: RowsBuilder<T>)  {

    @JvmSynthetic
    fun row(block: RowBuilderApi<T>.() -> Unit) = builder.addRowBuilder { RowBuilderApi.new(it).apply(block) }

    @JvmSynthetic
    fun row(at: Int, block: RowBuilderApi<T>.() -> Unit) = builder.addRowBuilder(at) {
        RowBuilderApi.new(it).apply(block)
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: RowsBuilder<T>): RowsBuilderApi<T> = RowsBuilderApi(builder)
    }
}

@TabulateMarker
class RowBuilderApi<T> private constructor(private val builder: RowBuilder<T>)  {

    @JvmSynthetic
    fun allMatching(predicate : RowPredicate<T>) = apply { builder.qualifier = RowQualifier(applyWhen = predicate) }

    @JvmSynthetic
    fun insertWhen(predicate : RowPredicate<T>) = apply { builder.qualifier = RowQualifier(createWhen = predicate) }

    @JvmSynthetic
    fun cells(block: CellsBuilderApi<T>.() -> Unit) = CellsBuilderApi.new(builder.cellsBuilder).apply(block)

    @JvmSynthetic
    fun attributes(block: RowLevelAttributesBuilderApi<T>.() -> Unit) = RowLevelAttributesBuilderApi(builder).apply(block)

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: RowBuilder<T>): RowBuilderApi<T> = RowBuilderApi(builder)
    }
}

@TabulateMarker
class CellsBuilderApi<T> private constructor(private val builder: CellsBuilder<T>) {

    @JvmSynthetic
    fun cell(id: String, block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder(id) {
        CellBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun cell(index: Int, block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder(index) {
        CellBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun cell(block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder {
        CellBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun cell(ref: ((record: T) -> Any?), block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder(ref) {
        CellBuilderApi.new(it).apply(block)
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: CellsBuilder<T>): CellsBuilderApi<T> = CellsBuilderApi(builder)
    }
}

@TabulateMarker
class CellBuilderApi<T> private constructor(private val builder: CellBuilder<T>) {

    @set:JvmSynthetic
    var value: Any? by builder::value

    @set:JvmSynthetic
    var expression: RowCellExpression<T>? by builder::expression

    @set:JvmSynthetic
    var type: CellType? by builder::type

    @set:JvmSynthetic
    var colSpan: Int by builder::colSpan

    @set:JvmSynthetic
    var rowSpan: Int by builder::rowSpan

    @JvmSynthetic
    fun attributes(block: CellLevelAttributesBuilderApi<T>.() -> Unit) = CellLevelAttributesBuilderApi(builder).apply(block)

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: CellBuilder<T>): CellBuilderApi<T> = CellBuilderApi(builder)
    }
}
