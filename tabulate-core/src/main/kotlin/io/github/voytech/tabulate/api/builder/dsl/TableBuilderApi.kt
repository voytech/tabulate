package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.api.builder.*
import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.template.context.DefaultSteps
import java.util.function.Consumer
import kotlin.reflect.KProperty1

@DslMarker
annotation class TabulateMarker

@JvmSynthetic
fun <T> table(block: TableBuilderApi<T>.() -> Unit): TableBuilder<T> {
    return TableBuilder<T>().also {
        TableBuilderApi.new(it).apply(block)
    }
}

object Table {
    @JvmSynthetic
    operator fun <T> invoke(block : TableBuilderApi<T>.() -> Unit)  = block
}

@JvmSynthetic
operator fun <T> (TableBuilderApi<T>.() -> Unit).plus(block : TableBuilderApi<T>.() -> Unit): (TableBuilderApi<T>.() -> Unit) {
    val self: (TableBuilderApi<T>.() -> Unit) = this
    return Table {
        self.invoke(this)
        block.invoke(this)
    }
}

@JvmSynthetic
fun <T> table(blocks: List<TableBuilderApi<T>.() -> Unit>): TableBuilder<T> {
    return TableBuilder<T>().also { builder ->
        blocks.forEach { block ->
            TableBuilderApi.new(builder).apply(block)
        }
    }
}

@JvmSynthetic
fun <T> table(vararg block: TableBuilderApi<T>.() -> Unit): TableBuilder<T> {
    return TableBuilder<T>().also { builder ->
        block.forEach { block ->
            TableBuilderApi.new(builder).apply(block)
        }
    }
}

@JvmSynthetic
fun createTable(block: TableBuilderApi<Unit>.() -> Unit): TableBuilder<Unit> {
    return TableBuilder<Unit>().also {
        TableBuilderApi.new(it).apply(block)
    }
}

@TabulateMarker
class TableLevelAttributesBuilderApi<T> internal constructor(private val builder: TableBuilder<T>) {

    @JvmSynthetic
    fun attribute(attribute: AttributeBuilder<*>) {
        builder.attribute(attribute)
    }

}

@TabulateMarker
class ColumnLevelAttributesBuilderApi<T> internal constructor(private val builder: ColumnBuilder<T>) {

    @JvmSynthetic
    fun <B: ColumnAttributeBuilder<A>,A: ColumnAttribute<A>> attribute(attributeBuilder: B) {
        builder.attribute(attributeBuilder)
    }

    @JvmSynthetic
    fun <B: CellAttributeBuilder<A>,A: CellAttribute<A>> attribute(attributeBuilder: B) {
        builder.attribute(attributeBuilder)
    }

}

@TabulateMarker
class RowLevelAttributesBuilderApi<T> internal constructor(private val builder: RowBuilder<T>) {

    @JvmSynthetic
    fun <B: RowAttributeBuilder<A>,A: RowAttribute<A>> attribute(attributeBuilder: B) {
        builder.attribute(attributeBuilder)
    }

    @JvmSynthetic
    fun <B: CellAttributeBuilder<A>,A: CellAttribute<A>> attribute(attributeBuilder: B) {
        builder.attribute(attributeBuilder)
    }
}

@TabulateMarker
class CellLevelAttributesBuilderApi<T> internal constructor(private val builder: CellBuilder<T>) {
    @JvmSynthetic
    fun <B: CellAttributeBuilder<A>,A: CellAttribute<A>> attribute(attributeBuilder: B) {
        builder.attribute(attributeBuilder)
    }
}


@TabulateMarker
class TableBuilderApi<T> private constructor(private val builder: TableBuilder<T>)  {

    @set:JvmSynthetic
    var name: String by this.builder::name

    @set:JvmSynthetic
    var firstRow: Int? by this.builder::firstRow

    @set:JvmSynthetic
    var firstColumn: Int? by this.builder::firstColumn

    @JvmSynthetic
    fun columns(block: ColumnsBuilderApi<T>.() -> Unit) {
        ColumnsBuilderApi(builder.columnsBuilder).apply(block)
    }

    @JvmSynthetic
    fun rows(block: RowsBuilderApi<T>.() -> Unit) {
        RowsBuilderApi(builder.rowsBuilder).apply(block)
    }

    @JvmSynthetic
    fun attributes(block: TableLevelAttributesBuilderApi<T>.() -> Unit) {
        TableLevelAttributesBuilderApi(builder).apply(block)
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
    fun column(id: String) {
        builder.addColumnBuilder(id) {}
    }

    @JvmSynthetic
    fun column(id: String, block: ColumnBuilderApi<T>.() -> Unit) {
        builder.addColumnBuilder(id) {
            ColumnBuilderApi.new(it).apply(block)
        }
    }

    @JvmSynthetic
    fun column(id: String, block: Consumer<ColumnBuilderApi<T>>) {
        builder.addColumnBuilder(id) {
            block.accept(ColumnBuilderApi.new(it))
        }
    }

    @JvmSynthetic
    fun column(ref: KProperty1<T,Any?>, block: ColumnBuilderApi<T>.() -> Unit) {
        builder.addColumnBuilder(ref.id()) {
            ColumnBuilderApi.new(it).apply(block)
        }
    }

    @JvmSynthetic
    fun column(ref: KProperty1<T,Any?>) {
        builder.addColumnBuilder(ref.id()) { }
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
    fun attributes(block: ColumnLevelAttributesBuilderApi<T>.() -> Unit) {
        ColumnLevelAttributesBuilderApi(builder).apply(block)
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: ColumnBuilder<T>): ColumnBuilderApi<T> = ColumnBuilderApi(builder)
    }
}

@TabulateMarker
class RowsBuilderApi<T> internal constructor(private val builder: RowsBuilder<T>)  {

    @JvmSynthetic
    fun row(block: RowBuilderApi<T>.() -> Unit) {
        builder.addRowBuilder { RowBuilderApi.new(it).apply(block) }
    }

    @JvmSynthetic
    fun row(at: Int, block: RowBuilderApi<T>.() -> Unit) {
        builder.addRowBuilder(RowIndexDef(at)) {
            RowBuilderApi.new(it).apply(block)
        }
    }

    @JvmSynthetic
    fun row(at: Int, label: DefaultSteps, block: RowBuilderApi<T>.() -> Unit) {
        builder.addRowBuilder(RowIndexDef(at,label.name)) {
            RowBuilderApi.new(it).apply(block)
        }
    }

    @JvmSynthetic
    fun row(label: DefaultSteps, block: RowBuilderApi<T>.() -> Unit) {
        builder.addRowBuilder(label) {
            RowBuilderApi.new(it).apply(block)
        }
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: RowsBuilder<T>): RowsBuilderApi<T> = RowsBuilderApi(builder)
    }
}

@TabulateMarker
class RowBuilderApi<T> private constructor(private val builder: RowBuilder<T>)  {

    @JvmSynthetic
    fun matching(predicate : RowPredicate<T>) = apply { builder.qualifier = RowQualifier(applyWhen = predicate) }

    @JvmSynthetic
    fun insertWhen(predicate : RowPredicate<T>) = apply { builder.qualifier = RowQualifier(createWhen = predicate) }

    @JvmSynthetic
    fun cells(block: CellsBuilderApi<T>.() -> Unit) {
        CellsBuilderApi.new(builder.cellsBuilder).apply(block)
    }

    @JvmSynthetic
    fun cell(id: String, block: CellBuilderApi<T>.() -> Unit) {
        cells {
            cell(id, block)
        }
    }

    @JvmSynthetic
    fun cell(index: Int, block: CellBuilderApi<T>.() -> Unit) {
        cells {
            cell(index, block)
        }
    }

    @JvmSynthetic
    fun cell(ref: KProperty1<T, Any?>, block: CellBuilderApi<T>.() -> Unit) {
        cells {
            cell(ref, block)
        }
    }

    @JvmSynthetic
    fun cell(block: CellBuilderApi<T>.() -> Unit) {
        cells {
            cell(block)
        }
    }

    @JvmSynthetic
    fun attributes(block: RowLevelAttributesBuilderApi<T>.() -> Unit) {
        RowLevelAttributesBuilderApi(builder).apply(block)
    }

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
    fun cell(ref: KProperty1<T, Any?>, block: CellBuilderApi<T>.() -> Unit) = builder.addCellBuilder(ref.id()) {
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
    fun attributes(block: CellLevelAttributesBuilderApi<T>.() -> Unit) {
        CellLevelAttributesBuilderApi(builder).apply(block)
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: CellBuilder<T>): CellBuilderApi<T> = CellBuilderApi(builder)
    }
}
