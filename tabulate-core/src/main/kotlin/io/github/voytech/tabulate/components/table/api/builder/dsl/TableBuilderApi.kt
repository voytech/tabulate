package io.github.voytech.tabulate.components.table.api.builder.dsl

import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.container.api.builder.dsl.ContainerBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.*
import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.model.attributes.cell.cellType
import io.github.voytech.tabulate.components.table.template.AdditionalSteps
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.WrapperBuilderApi
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.ModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.api.builder.dsl.buildModel
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.DataSourceBinding
import kotlin.reflect.KProperty1
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.CellType as TypeHint


/**
 * Entry point function taking type-safe DSL table builder API as a parameter.
 * Applies all table builder API instructions into managed [TableBuilderState].
 * @return [TableBuilderState]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@JvmSynthetic
internal fun <T: Any> createTableBuilder(block: TableBuilderApi<T>.() -> Unit): TableBuilderState<T> =
    TableBuilderApi<T>().apply(block).builder


/**
 * Entry point function taking type-safe DSL table builder API as a parameter.
 * Materializes internal table builder state and returns read-only [io.github.voytech.tabulate.model.Table] model.
 * @return [io.github.voytech.tabulate.model.Table]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun <T: Any> createTable(block: TableBuilderApi<T>.() -> Unit): Table<T> =
    buildModel(TableBuilderApi<T>().apply(block))

/**
 * Entry point function taking type-safe DSL table builder API as a parameter.
 * Function goals - wrapping DSL builder and providing lambda receiver type, inferring type of right-hand side table
 * declaration for inline table merging.
 * @return lambda receiver block
 * @author Wojciech Mąka
 * @since 0.2.0
 */
fun <T: Any> typedTable(block: TableBuilderApi<T>.() -> Unit): TableBuilderApi<T>.() -> Unit = block

/**
 * Entry point function taking type-safe DSL table builder API as a parameter.
 * Main goal of this function is to wrap DSL builder and provide lambda receiver type.
 * @return lambda receiver block
 * @author Wojciech Mąka
 * @since 0.2.0
 */
fun table(block: TableBuilderApi<Unit>.() -> Unit): TableBuilderApi<Unit>.() -> Unit = block

//TODO introduce api marker on which API builders below can be installed in one shot

fun <T: Any> DocumentBuilderApi.table(block: TableBuilderApi<T>.() -> Unit) = bind(TableBuilderApi<T>().apply(block))

fun <T: Any> PageBuilderApi.table(block: TableBuilderApi<T>.() -> Unit) = bind(TableBuilderApi<T>().apply(block))

fun <T: Any> ContainerBuilderApi.table(block: TableBuilderApi<T>.() -> Unit) = bind(TableBuilderApi<T>().apply(block))

infix fun <T: Any> WrapperBuilderApi.table(block: TableBuilderApi<T>.() -> Unit) = bind(TableBuilderApi<T>().apply(block))


/**
 * Plus operator for merging multiple table DSL builders.
 * @return lambda receiver block
 * @author Wojciech Mąka
 * @since 0.2.0
 */
@Suppress("UNCHECKED_CAST")
@JvmSynthetic
operator fun <T: Any,E: Any> (TableBuilderApi<E>.() -> Unit).plus(block: TableBuilderApi<T>.() -> Unit): (TableBuilderApi<T>.() -> Unit) {
    val self: (TableBuilderApi<E>.() -> Unit) = this
    return typedTable {
        self.invoke(this as TableBuilderApi<E>)
        block.invoke(this)
    }
}

/**
 * Kotlin type-safe DSL table attribute builder API for defining table level attributes.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class TableLevelAttributesBuilderApi<T: Any> internal constructor(private val builderState: TableBuilderState<T>) {

    @JvmSynthetic
    fun <B : AttributeBuilder<A>, A : Attribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }

}

/**
 * Kotlin type-safe DSL column attribute builder API for defining column level attributes.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class ColumnLevelAttributesBuilderApi<T: Any> internal constructor(private val builderState: ColumnBuilderState<T>) {
    @JvmSynthetic
    fun <B : AttributeBuilder<A>, A : Attribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }

}

/**
 * Kotlin type-safe DSL row attribute builder API for defining row level attributes.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class RowLevelAttributesBuilderApi<T: Any> internal constructor(private val builderState: RowBuilderState<T>) {

    @JvmSynthetic
    fun <B : AttributeBuilder<A>, A : Attribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }

}

/**
 * Kotlin type-safe DSL cell attribute builder API for defining cell level attributes.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class CellLevelAttributesBuilderApi<T: Any> internal constructor(private val builderState: CellBuilderState<T>) {
    @JvmSynthetic
    fun <B : AttributeBuilder<A>, A : Attribute<A>> attribute(attributeBuilder: B) {
        builderState.attribute(attributeBuilder)
    }

}

/**
 * Kotlin type-safe DSL table builder API for defining entire table.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class TableBuilderApi<T: Any> internal constructor() : ModelBuilderApi<Table<T>, TableBuilderState<T>>(
    TableBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var name: String by this.builder::name

    @set:JvmSynthetic
    @get:JvmSynthetic
    var firstRow: Int? by this.builder::firstRow

    @set:JvmSynthetic
    @get:JvmSynthetic
    var firstColumn: Int? by this.builder::firstColumn


    fun dataSource(dataSource: Collection<T>)  {
        this.builder.dataSource = DataSourceBinding(dataSource)
    }

    @JvmSynthetic
    fun columns(block: ColumnsBuilderApi<T>.() -> Unit) {
        ColumnsBuilderApi(builder.columnsBuilderState).apply(block)
    }

    @JvmSynthetic
    fun columns(vararg refs: KProperty1<T, Any?>) {
        ColumnsBuilderApi(builder.columnsBuilderState).apply {
            refs.forEach { column(it) }
        }
    }


    @JvmSynthetic
    fun rows(block: RowsBuilderApi<T>.() -> Unit) {
        RowsBuilderApi(builder.rowsBuilderState).apply(block)
    }

    @JvmSynthetic
    fun attributes(block: TableLevelAttributesBuilderApi<T>.() -> Unit) {
        TableLevelAttributesBuilderApi(builder).apply(block)
    }
}

/**
 * Kotlin type-safe DSL columns builder API for columns.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class ColumnsBuilderApi<T: Any> internal constructor(private val builderState: ColumnBuilderStateCollection<T>) {

    @JvmSynthetic
    fun column(id: String) {
        builderState.ensureColumnBuilder(id) {}
    }

    @JvmSynthetic
    fun column(id: String, block: ColumnBuilderApi<T>.() -> Unit) {
        builderState.ensureColumnBuilder(id) {
            ColumnBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun column(index: Int, block: ColumnBuilderApi<T>.() -> Unit) {
        builderState.ensureColumnBuilder(index) {
            ColumnBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun column(ref: KProperty1<T, Any?>, block: ColumnBuilderApi<T>.() -> Unit) {
        builderState.ensureColumnBuilder(ref.id()) {
            ColumnBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun column(ref: KProperty1<T, Any?>) {
        builderState.ensureColumnBuilder(ref.id()) { }
    }
}

/**
 * Kotlin type-safe DSL column builder API for defining single column.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class ColumnBuilderApi<T: Any> internal constructor(private val builderState: ColumnBuilderState<T>) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var index: Int by builderState::index


    @JvmSynthetic
    fun attributes(block: ColumnLevelAttributesBuilderApi<T>.() -> Unit) {
        ColumnLevelAttributesBuilderApi(builderState).apply(block)
    }
}

/**
 * [RowPredicateBuilderApi] simplifies row predicate construction as it brings all default [RowPredicate] methods into scope of
 * lambda (lambda with receiver). Consumer of an API do not need to know [RowPredicate] methods and do not need to import
 * them explicitly. Goal here is to make using DSL builder API as intuitive as it only can be.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowPredicateBuilderApi<T> {
    @JvmSynthetic
    fun all(): RowPredicate<T> = RowPredicates.all()

    @JvmSynthetic
    fun even(): RowPredicate<T> = RowPredicates.even<T>()

    @JvmSynthetic
    fun odd(): RowPredicate<T> = RowPredicates.odd<T>()

    @JvmSynthetic
    fun eq(rowIndex: Int): RowPredicate<T> = RowPredicates.eq(rowIndex)

    @JvmSynthetic
    fun lt(rowIndex: Int): RowPredicate<T> = RowPredicates.lt(rowIndex)

    @JvmSynthetic
    fun gt(rowIndex: Int): RowPredicate<T> = RowPredicates.gt(rowIndex)

    @JvmSynthetic
    fun gte(rowIndex: Int): RowPredicate<T> = RowPredicates.gte(rowIndex)

    @JvmSynthetic
    fun lte(rowIndex: Int): RowPredicate<T> = RowPredicates.lte(rowIndex)

    @JvmSynthetic
    fun eq(rowIndex: Int, steps: Enum<*>): RowPredicate<T> = RowPredicates.eq(rowIndex, steps)

    @JvmSynthetic
    fun header(): RowPredicate<T> = eq(0)

    @JvmSynthetic
    fun footer(): RowPredicate<T> = eq(0, AdditionalSteps.TRAILING_ROWS)

    @JvmSynthetic
    fun record(listIndex: Int): RowPredicate<T> = RowPredicates.record(listIndex)

    @JvmSynthetic
    fun records(): RowPredicate<T> = RowPredicates.records()

    @JvmSynthetic
    fun matching(provider: () -> RowPredicate<T>): RowPredicate<T> = provider()
}

/**
 * [RowIndexPredicateBuilderApi] simplifies row index literal predicate construction as it brings all default methods into scope of
 * lambda (lambda with receiver). Consumer of an API do not need to know predicate methods in advance and do not need to import
 * them explicitly. Goal here is to make using DSL builder API as intuitive as it only can be.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowIndexPredicateBuilderApi {

    @JvmSynthetic
    fun even(): OperatorBasedIndexPredicateLiteral = TODO("figure out")

    @JvmSynthetic
    fun odd(): OperatorBasedIndexPredicateLiteral = TODO("figure out")

    @JvmSynthetic
    fun eq(rowIndex: Int, step: Enum<*>? = null): OperatorBasedIndexPredicateLiteral =
        io.github.voytech.tabulate.components.table.model.eq(rowIndex, step)

    @JvmSynthetic
    fun lt(rowIndex: Int, step: Enum<*>? = null): OperatorBasedIndexPredicateLiteral =
        io.github.voytech.tabulate.components.table.model.lt(rowIndex, step)

    @JvmSynthetic
    fun gt(rowIndex: Int, step: Enum<*>? = null): OperatorBasedIndexPredicateLiteral =
        io.github.voytech.tabulate.components.table.model.gt(rowIndex, step)

    @JvmSynthetic
    fun gte(rowIndex: Int, step: Enum<*>? = null): OperatorBasedIndexPredicateLiteral =
        io.github.voytech.tabulate.components.table.model.gte(rowIndex, step)

    @JvmSynthetic
    fun lte(rowIndex: Int, step: Enum<*>? = null): OperatorBasedIndexPredicateLiteral =
        io.github.voytech.tabulate.components.table.model.lte(rowIndex, step)

    @JvmSynthetic
    fun header(): OperatorBasedIndexPredicateLiteral = eq(0)

    @JvmSynthetic
    fun footer(): OperatorBasedIndexPredicateLiteral = eq(0, AdditionalSteps.TRAILING_ROWS)

}

/**
 * Kotlin type-safe DSL rows builder API for defining rows.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class RowsBuilderApi<T: Any> internal constructor(private val builderState: RowBuilderStateCollection<T>) {

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
    fun newRow(predicate: PredicateLiteral, block: RowBuilderApi<T>.() -> Unit) {
        builderState.addRowBuilder(RowIndexPredicateLiteral(predicate)) {
            RowBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun atIndex(predicateBlock: RowIndexPredicateBuilderApi.() -> PredicateLiteral) = predicateBlock

    @JvmName("newRowAtIndex")
    @JvmSynthetic
    infix fun (RowIndexPredicateBuilderApi.() -> PredicateLiteral).newRow(block: RowBuilderApi<T>.() -> Unit) {
        newRow(this(RowIndexPredicateBuilderApi()), block)
    }


    @JvmSynthetic
    fun row(predicate: RowPredicate<T>, block: RowBuilderApi<T>.() -> Unit) {
        builderState.addRowBuilder(predicate) {
            RowBuilderApi(it).apply(block)
        }
    }

    @JvmSynthetic
    fun matching(predicateBlock: RowPredicateBuilderApi<T>.() -> RowPredicate<T>) = predicateBlock

    @JvmSynthetic
    infix fun (RowPredicateBuilderApi<T>.() -> RowPredicate<T>).assign(block: RowBuilderApi<T>.() -> Unit) {
        row(this(RowPredicateBuilderApi()), block)
    }
}

/**
 * Kotlin type-safe DSL row builder API for defining single row.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class RowBuilderApi<T: Any> internal constructor(private val builderState: RowBuilderState<T>) {

    @JvmSynthetic
    fun cells(block: CellsBuilderApi<T>.() -> Unit) {
        CellsBuilderApi(builderState.cellBuilderStateCollection).apply(block)
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

/**
 * Kotlin type-safe DSL cells builder API for defining row cells.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class CellsBuilderApi<T: Any> internal constructor(private val builderState: CellBuilderStateCollection<T>) {

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

/**
 * Kotlin type-safe DSL cell builder API for defining single row cell.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class CellBuilderApi<T: Any> internal constructor(private val builderState: CellBuilderState<T>) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var value: Any? by builderState::value

    @set:JvmSynthetic
    @get:JvmSynthetic
    var expression: RowCellExpression<T>? by builderState::expression

    @set:JvmSynthetic
    @get:JvmSynthetic
    var colSpan: Int by builderState::colSpan

    @set:JvmSynthetic
    @get:JvmSynthetic
    var rowSpan: Int by builderState::rowSpan

    @JvmSynthetic
    fun typeHint(block: () -> TypeHint) {
        attributes { cellType(block) }
    }

    @JvmSynthetic
    fun attributes(block: CellLevelAttributesBuilderApi<T>.() -> Unit) {
        CellLevelAttributesBuilderApi(builderState).apply(block)
    }
}
