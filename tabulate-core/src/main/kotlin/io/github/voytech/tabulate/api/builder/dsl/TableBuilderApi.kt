package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.api.builder.*
import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import java.util.function.Consumer
import kotlin.reflect.KProperty1

@DslMarker
annotation class TabulateMarker

@JvmSynthetic
internal fun <T> createTableBuilder(block: TableBuilderApi<T>.() -> Unit): TableBuilderState<T> {
    return TableBuilderState<T>().also {
        TableBuilderApi(it).apply(block)
    }
}

object Table {
    @JvmSynthetic
    operator fun <T> invoke(block: TableBuilderApi<T>.() -> Unit) = block
}

@JvmSynthetic
operator fun <T> (TableBuilderApi<T>.() -> Unit).plus(block: TableBuilderApi<T>.() -> Unit): (TableBuilderApi<T>.() -> Unit) {
    val self: (TableBuilderApi<T>.() -> Unit) = this
    return Table {
        self.invoke(this)
        block.invoke(this)
    }
}

@TabulateMarker
class TableLevelAttributesBuilderApi<T> internal constructor(private val builderState: TableBuilderState<T>) {

    @JvmSynthetic
    fun attribute(attribute: AttributeBuilder<*>) {
        builderState.attribute(attribute)
    }

}

@TabulateMarker
class ColumnLevelAttributesBuilderApi<T> internal constructor(private val builderState: ColumnBuilderState<T>) {

    @JvmSynthetic
    fun <B : ColumnAttributeBuilder<A>, A : ColumnAttribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }

    @JvmSynthetic
    fun <B : CellAttributeBuilder<A>, A : CellAttribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }

}

@TabulateMarker
class RowLevelAttributesBuilderApi<T> internal constructor(private val builderState: RowBuilderState<T>) {

    @JvmSynthetic
    fun <B : RowAttributeBuilder<A>, A : RowAttribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }

    @JvmSynthetic
    fun <B : CellAttributeBuilder<A>, A : CellAttribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }
}

@TabulateMarker
class CellLevelAttributesBuilderApi<T> internal constructor(private val builderState: CellBuilderState<T>) {
    @JvmSynthetic
    fun <B : CellAttributeBuilder<A>, A : CellAttribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }
}


@TabulateMarker
class TableBuilderApi<T> internal constructor(private val builderState: TableBuilderState<T>) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var name: String by this.builderState::name

    @set:JvmSynthetic
    @get:JvmSynthetic
    var firstRow: Int? by this.builderState::firstRow

    @set:JvmSynthetic
    @get:JvmSynthetic
    var firstColumn: Int? by this.builderState::firstColumn

    @JvmSynthetic
    fun columns(block: ColumnsBuilderApi<T>.() -> Unit) {
        ColumnsBuilderApi(builderState.columnsBuilderState).apply(block)
    }

    @JvmSynthetic
    fun rows(block: RowsBuilderApi<T>.() -> Unit) {
        RowsBuilderApi(builderState.rowsBuilderState).apply(block)
    }

    @JvmSynthetic
    fun attributes(block: TableLevelAttributesBuilderApi<T>.() -> Unit) {
        TableLevelAttributesBuilderApi(builderState).apply(block)
    }
}

@TabulateMarker
class ColumnsBuilderApi<T> internal constructor(private val builderState: ColumnsBuilderState<T>) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var count: Int? by this.builderState::count

    @JvmSynthetic
    fun column(id: String) {
        builderState.addColumnBuilder(id) {}
    }

    @JvmSynthetic
    fun column(id: String, block: ColumnBuilderApi<T>.() -> Unit) {
        builderState.addColumnBuilder(id) {
            ColumnBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun column(id: String, block: Consumer<ColumnBuilderApi<T>>) {
        builderState.addColumnBuilder(id) {
            block.accept(ColumnBuilderApi(it))
        }
    }

    @JvmSynthetic
    fun column(ref: KProperty1<T, Any?>, block: ColumnBuilderApi<T>.() -> Unit) {
        builderState.addColumnBuilder(ref.id()) {
            ColumnBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun column(ref: KProperty1<T, Any?>) {
        builderState.addColumnBuilder(ref.id()) { }
    }
}

@TabulateMarker
class ColumnBuilderApi<T> internal constructor(private val builderState: ColumnBuilderState<T>) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var columnType: CellType? by builderState::columnType

    @set:JvmSynthetic
    @get:JvmSynthetic
    var index: Int by builderState::index

    @JvmSynthetic
    fun attributes(block: ColumnLevelAttributesBuilderApi<T>.() -> Unit) {
        ColumnLevelAttributesBuilderApi(builderState).apply(block)
    }
}

@TabulateMarker
class RowsBuilderApi<T> internal constructor(private val builderState: RowsBuilderState<T>) {

    @JvmSynthetic
    fun newRow(block: RowBuilderApi<T>.() -> Unit) {
        builderState.addRowBuilder { RowBuilderApi(it).apply(block) }
    }

    @JvmSynthetic
    fun newRow(at: Int, block: RowBuilderApi<T>.() -> Unit) {
        builderState.addRowBuilder(RowIndexDef(at)) {
            RowBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun newRow(predicate: PredicateLiteral, block: RowBuilderApi<T>.() -> Unit) {
        builderState.addRowBuilder(RowIndexPredicateLiteral(predicate)) {
            RowBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun newRow(at: Int, step: Enum<*>, block: RowBuilderApi<T>.() -> Unit) {
        builderState.addRowBuilder(RowIndexDef(at, step)) {
            RowBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun newRow(step: Enum<*>, block: RowBuilderApi<T>.() -> Unit) {
        builderState.addRowBuilder(step) {
            RowBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun row(predicate: RowPredicate<T>, block: RowBuilderApi<T>.() -> Unit) {
        builderState.addRowBuilder(predicate) {
            RowBuilderApi(it).apply(block)
        }
    }
}


@TabulateMarker
class RowBuilderApi<T> internal constructor(private val builderState: RowBuilderState<T>) {

    @JvmSynthetic
    fun cells(block: CellsBuilderApi<T>.() -> Unit) {
        CellsBuilderApi(builderState.cellsBuilderState).apply(block)
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
        RowLevelAttributesBuilderApi(builderState).apply(block)
    }

}

@TabulateMarker
class CellsBuilderApi<T> internal constructor(private val builderState: CellsBuilderState<T>) {

    @JvmSynthetic
    fun cell(id: String, block: CellBuilderApi<T>.() -> Unit) {
        builderState.addCellBuilder(id) {
            CellBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun cell(index: Int, block: CellBuilderApi<T>.() -> Unit) {
        builderState.addCellBuilder(index) {
            CellBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun cell(block: CellBuilderApi<T>.() -> Unit) {
        builderState.addCellBuilder {
            CellBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun cell(ref: KProperty1<T, Any?>, block: CellBuilderApi<T>.() -> Unit) {
        builderState.addCellBuilder(ref.id()) {
            CellBuilderApi(it).apply(block)
        }
    }

}

@TabulateMarker
class CellBuilderApi<T> internal constructor(private val builderState: CellBuilderState<T>) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var value: Any? by builderState::value

    @set:JvmSynthetic
    @get:JvmSynthetic
    var expression: RowCellExpression<T>? by builderState::expression

    @set:JvmSynthetic
    @get:JvmSynthetic
    var type: CellType? by builderState::type

    @set:JvmSynthetic
    @get:JvmSynthetic
    var colSpan: Int by builderState::colSpan

    @set:JvmSynthetic
    @get:JvmSynthetic
    var rowSpan: Int by builderState::rowSpan

    @JvmSynthetic
    fun attributes(block: CellLevelAttributesBuilderApi<T>.() -> Unit) {
        CellLevelAttributesBuilderApi(builderState).apply(block)
    }
}
