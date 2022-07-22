package io.github.voytech.tabulate.components.table.api.builder.fluent

import io.github.voytech.tabulate.components.table.api.builder.*
import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.TypeHintAttribute
import io.github.voytech.tabulate.core.model.Attribute
import java.util.concurrent.Callable
import java.util.function.Consumer
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.CellType as TypeHint

/**
 * Base class for all java fluent table builders.
 * Provides methods for navigating builders hierarchy.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed class FluentTableBuilderApi<T: Any> {

    @JvmSynthetic
    internal abstract fun up(): FluentTableBuilderApi<T>

    fun root(): TableBuilder<T> {
        var upper: FluentTableBuilderApi<T> = this
        while (upper.javaClass != TableBuilder::class.java) {
            upper = upper.up()
        }
        return upper as TableBuilder<T>
    }

    fun build() : Table<T> = root().builderState.build()
}

interface RowBuilderMethods<T: Any> {
    fun row(): RowBuilder<T>
    fun row(at: Int): RowBuilder<T>
    fun row(at: Int, offset: Enum<*>): RowBuilder<T>
    fun row(predicate: RowPredicate<T>): RowBuilder<T>
}

interface CellBuilderMethods<T: Any> {
    fun cell(): CellBuilder<T>
    fun cell(id: String): CellBuilder<T>
    fun cell(key: String, reference: java.util.function.Function<T, Any?>): CellBuilder<T>
    fun cell(index: Int): CellBuilder<T>
}

interface ColumnsBuilderMethods<T: Any> {
    fun column(id: String): ColumnBuilder<T>
    fun column(key: String, reference: java.util.function.Function<T, Any?>): ColumnBuilder<T>
}

/**
 * Java fluent table builder API for defining entier table model.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableBuilder<T: Any> : FluentTableBuilderApi<T>() {

    @get:JvmSynthetic
    internal val builderState: TableBuilderState<T> = TableBuilderState()

    @get:JvmSynthetic
    internal val cache: PropertyReferencesCache = PropertyReferencesCache()

    fun name(name: String) = apply {
        this.builderState.name = name
    }

    fun firstRow(firstRow: Int?) = apply {
        this.builderState.firstRow = firstRow
    }

    fun firstColumn(firstColumn: Int?) = apply {
        this.builderState.firstColumn = firstColumn
    }

    fun columns() = ColumnsBuilder(this)

    fun rows() = RowsBuilder(this)

    fun <A : Attribute<A>, B : io.github.voytech.tabulate.core.api.builder.AttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    fun <A : Attribute<A>, B : io.github.voytech.tabulate.core.api.builder.AttributeBuilder<A>> attribute(
        attributeProvider: Callable<B>,
        attributeConfigurer: Consumer<B>,
    ) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmSynthetic
    override fun up(): FluentTableBuilderApi<T> = this
}

/**
 * Java fluent columns builder API for defining columns.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ColumnsBuilder<T: Any> internal constructor(private val parent: TableBuilder<T>) :
    FluentTableBuilderApi<T>(), ColumnsBuilderMethods<T> {

    override fun column(id: String) =
        ColumnBuilder(parent.builderState.columnsBuilderState.ensureColumnBuilder(id) {}, this)

    override fun column(key: String, reference: java.util.function.Function<T, Any?>) =
        ColumnBuilder(parent.builderState.columnsBuilderState.ensureColumnBuilder(
            parent.cache.cached(NamedPropertyReferenceColumnKey(key,reference))
        ) {}, this)

    @JvmSynthetic
    override fun up(): TableBuilder<T> = parent

}

/**
 * Java fluent column builder API for defining single column.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ColumnBuilder<T: Any> internal constructor(
    private val builderState: ColumnBuilderState<T>,
    private val parent: ColumnsBuilder<T>,
) : FluentTableBuilderApi<T>(), ColumnsBuilderMethods<T> by parent {

    fun index(index: Int) = apply {
        builderState.index = index
    }

    fun rows() = up().up().rows()

    @JvmName("columnAttribute")
    fun <A : ColumnAttribute<A>, B : ColumnAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    @JvmName("columnAttribute")
    fun <A : ColumnAttribute<A>, B : ColumnAttributeBuilder<A>> attribute(
        attributeProvider: Callable<B>,
        attributeConfigurer: Consumer<B>,
    ) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmName("cellAttribute")
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    @JvmName("cellAttribute")
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(
        attributeProvider: Callable<B>,
        attributeConfigurer: Consumer<B>,
    ) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmSynthetic
    override fun up(): ColumnsBuilder<T> = parent
}

/**
 * Java fluent rows builder API for defining rows.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowsBuilder<T: Any> internal constructor(
    private val parent: TableBuilder<T>,
) : FluentTableBuilderApi<T>(), RowBuilderMethods<T> {

    override fun row() = RowBuilder(parent.builderState.rowsBuilderState.addRowBuilder(), this)

    override fun row(at: Int) =
        RowBuilder(parent.builderState.rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(eq(at))), this)

    override fun row(at: Int, offset: Enum<*>) =
        RowBuilder(parent.builderState.rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(eq(at, offset))), this)

    override fun row(predicate: RowPredicate<T>) =
        RowBuilder(parent.builderState.rowsBuilderState.addRowBuilder(predicate), this)

    @JvmSynthetic
    override fun up(): TableBuilder<T> = parent

}

/**
 * Java fluent row builder API for defining single row.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowBuilder<T: Any> internal constructor(
    private val builderState: RowBuilderState<T>,
    private val parent: RowsBuilder<T>,
) : FluentTableBuilderApi<T>(),
    RowBuilderMethods<T> by parent,
    CellBuilderMethods<T> {

    override fun cell() = CellBuilder(builderState.cellBuilderStateCollection.addCellBuilder { }, this)

    override fun cell(id: String) = CellBuilder(
        root().cache.cached<T>(id)?.let {
            builderState.cellBuilderStateCollection.addCellBuilder(it) {}
        } ?: builderState.cellBuilderStateCollection.addCellBuilder(id) {},
        this
    )

    override fun cell(key: String, reference: java.util.function.Function<T, Any?>) =
        CellBuilder(builderState.cellBuilderStateCollection.addCellBuilder(
            root().cache.cached(NamedPropertyReferenceColumnKey(key,reference))
        ) {}, this)

    override fun cell(index: Int): CellBuilder<T> =
        CellBuilder(builderState.cellBuilderStateCollection.addCellBuilder(index) {}, this)

    @JvmName("rowAttribute")
    fun <A : RowAttribute<A>, B : RowAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    @JvmName("rowAttribute")
    fun <A : RowAttribute<A>, B : RowAttributeBuilder<A>> attribute(
        attributeProvider: Callable<B>,
        attributeConfigurer: Consumer<B>,
    ) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmName("cellAttribute")
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    @JvmName("cellAttribute")
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(
        attributeProvider: Callable<B>,
        attributeConfigurer: Consumer<B>,
    ) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmSynthetic
    override fun up(): RowsBuilder<T> = parent
}

/**
 * Java fluent cell builder API for defining single row cell.
 * Internally operates on corresponding builder state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellBuilder<T: Any> internal constructor(
    private val builderState: CellBuilderState<T>,
    private val parent: RowBuilder<T>,
) : FluentTableBuilderApi<T>(),
    RowBuilderMethods<T> by parent,
    CellBuilderMethods<T> by parent {

    fun value(value: Any?) = apply {
        builderState.value = value
    }

    fun eval(expression: RowCellExpression<T>?) = apply {
        builderState.expression = expression
    }

    fun type(type: TypeHint) = apply {
        builderState.attribute(TypeHintAttribute.Builder().apply { this.type = type })
    }

    fun colSpan(colSpan: Int) = apply {
        builderState.colSpan = colSpan
    }

    fun rowSpan(rowSpan: Int) = apply {
        builderState.rowSpan = rowSpan
    }

    @JvmName("cellAttribute")
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        builderState.attribute(attributeProvider.call())
    }

    @JvmName("cellAttribute")
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(
        attributeProvider: Callable<B>,
        attributeConfigurer: Consumer<B>,
    ) = apply {
        builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmSynthetic
    override fun up(): RowBuilder<T> = parent
}
