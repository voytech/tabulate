package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.api.builder.*
import io.github.voytech.tabulate.model.CellType
import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.RowCellEval
import io.github.voytech.tabulate.model.RowSelector
import io.github.voytech.tabulate.model.attributes.Attribute

@DslMarker
annotation class TabulateMarker

@JvmSynthetic
fun <T> table(block: TableBuilderApi<T>.() -> Unit): TableBuilder<T> {
    return TableBuilder<T>().also {
        TableBuilderApi.new(it).apply(block)
    }
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
    fun attributes(vararg attributes: Attribute<*>) {
        builder.attributes(attributes.asList())
    }

    @JvmSynthetic
    fun attributes(attributes: List<Attribute<*>>) {
        builder.attributes(attributes)
    }

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
        ColumnBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?), block: ColumnBuilderApi<T>.() -> Unit) = builder.addColumnBuilder(ref) {
        ColumnBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?)) = builder.addColumnBuilder(ref) {}

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

    @set:JvmSynthetic
    var dataFormatter: ((field: Any) -> Any)? by builder::dataFormatter

    @JvmSynthetic
    fun attributes(vararg attributes: Attribute<*>) {
        builder.attributes(attributes.asList())
    }

    @JvmSynthetic
    fun attributes(attributes: List<Attribute<*>>) {
        builder.attributes(attributes)
    }

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
    fun row(selector: RowSelector<T>, block: RowBuilderApi<T>.() -> Unit) = builder.addRowBuilder(selector) {
        RowBuilderApi.new(it).apply(block)
    }

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

    @set:JvmSynthetic
    var createAt: Int? by builder::createAt

    @set:JvmSynthetic
    var selector: RowSelector<T>? by builder::selector

    @JvmSynthetic
    fun cells(block: CellsBuilderApi<T>.() -> Unit) = CellsBuilderApi.new(builder.cellsBuilder).apply(block)

    @JvmSynthetic
    fun attributes(vararg attributes: Attribute<*>) {
        builder.attributes(attributes.asList())
    }

    @JvmSynthetic
    fun attributes(attributes: List<Attribute<*>>) {
        builder.attributes(attributes)
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: RowBuilder<T>): RowBuilderApi<T> = RowBuilderApi(builder)
    }
}

@TabulateMarker
class CellsBuilderApi<T> private constructor(private val builder: CellsBuilder<T>) {

    @JvmSynthetic
    fun forColumn(id: String, block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder(id) {
        CellBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun cell(index: Int, block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder(index) {
        CellBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun cell(block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder() {
        CellBuilderApi.new(it).apply(block)
    }

    @JvmSynthetic
    fun forColumn(ref: ((record: T) -> Any?), block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder(ref) {
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
    var eval: RowCellEval<T>? by builder::eval

    @set:JvmSynthetic
    var type: CellType? by builder::type

    @set:JvmSynthetic
    var colSpan: Int by builder::colSpan

    @set:JvmSynthetic
    var rowSpan: Int by builder::rowSpan

    @JvmSynthetic
    fun attributes(vararg attributes: Attribute<*>) {
        builder.attributes(attributes.asList())
    }

    @JvmSynthetic
    fun attributes(attributes: List<Attribute<*>>) {
        builder.attributes(attributes)
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: CellBuilder<T>): CellBuilderApi<T> = CellBuilderApi(builder)
    }
}
