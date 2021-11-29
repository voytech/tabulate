package io.github.voytech.tabulate.api.builder.fluent

import io.github.voytech.tabulate.api.builder.*
import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.model.attributes.cell.TypeHintAttribute
import java.util.concurrent.Callable
import java.util.function.Consumer
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.CellType as TypeHint

sealed class FluentTableBuilderApi<T> {

    @JvmSynthetic
    internal abstract fun up(): FluentTableBuilderApi<T>

    @JvmSynthetic
    internal fun root(): TableBuilderState<T> {
        var upper: FluentTableBuilderApi<T> = this
        while (upper.javaClass != TableBuilder::class.java) {
            upper = upper.up()
        }
        return (upper as TableBuilder<T>).builderState
    }
}

interface RowBuilderMethods<T> {
    fun row(): RowBuilder<T>
    fun row(at: Int): RowBuilder<T>
    fun row(at: Int, offset: Enum<*>): RowBuilder<T>
    fun row(predicate: RowPredicate<T>): RowBuilder<T>
}

interface CellBuilderMethods<T> {
    fun cell(): CellBuilder<T>
    fun cell(id: String): CellBuilder<T>
    fun cell(ref: NamedPropertyReferenceColumnKey<T>): CellBuilder<T>
    fun cell(index: Int): CellBuilder<T>
}

interface ColumnsBuilderMethods<T> {
    fun column(id: String): ColumnBuilder<T>
    fun column(ref: NamedPropertyReferenceColumnKey<T>): ColumnBuilder<T>
}

class TableBuilder<T> : FluentTableBuilderApi<T>() {

    @get:JvmSynthetic
    internal val builderState: TableBuilderState<T> = TableBuilderState()

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

    fun <A : Attribute<A>, B : AttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    fun <A : Attribute<A>, B : AttributeBuilder<A>> attribute(
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

class ColumnsBuilder<T> internal constructor(private val parent: TableBuilder<T>) :
    FluentTableBuilderApi<T>(), ColumnsBuilderMethods<T> {

    override fun column(id: String) =
        ColumnBuilder(parent.builderState.columnsBuilderState.ensureColumnBuilder(id) {}, this)

    override fun column(ref: NamedPropertyReferenceColumnKey<T>) =
        ColumnBuilder(parent.builderState.columnsBuilderState.ensureColumnBuilder(ref) {}, this)

    @JvmSynthetic
    override fun up(): TableBuilder<T> = parent

}

class ColumnBuilder<T> internal constructor(
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

class RowsBuilder<T> internal constructor(
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

class RowBuilder<T> internal constructor(
    private val builderState: RowBuilderState<T>,
    private val parent: RowsBuilder<T>,
) : FluentTableBuilderApi<T>(),
    RowBuilderMethods<T> by parent,
    CellBuilderMethods<T> {

    override fun cell() = CellBuilder(builderState.cellsBuilderState.addCellBuilder { }, this)

    override fun cell(id: String) = CellBuilder(builderState.cellsBuilderState.addCellBuilder(id) {}, this)

    override fun cell(ref: NamedPropertyReferenceColumnKey<T>) =
        CellBuilder(builderState.cellsBuilderState.addCellBuilder(ref) {}, this)

    override fun cell(index: Int): CellBuilder<T> =
        CellBuilder(builderState.cellsBuilderState.addCellBuilder(index) {}, this)

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

class CellBuilder<T> internal constructor(
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
