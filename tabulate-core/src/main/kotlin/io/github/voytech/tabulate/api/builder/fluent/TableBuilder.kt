package io.github.voytech.tabulate.api.builder.fluent

import io.github.voytech.tabulate.api.builder.*
import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.template.context.DefaultSteps
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.function.Function as JFunction

sealed class FluentTableBuilderApi<T>  {

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

class ColumnsBuilder<T> internal constructor(private val parent: TableBuilder<T>) : FluentTableBuilderApi<T>() {

    fun column(id: String) = ColumnBuilder(parent.builderState.columnsBuilderState.addColumnBuilder(id) {
    }, this)

    fun column(ref: JFunction<T, Any?>) = ColumnBuilder(parent.builderState.columnsBuilderState.addColumnBuilder(ref.id()) {
    }, this)

    @JvmSynthetic
    override fun up(): TableBuilder<T> = parent

}

class ColumnBuilder<T> internal constructor(
    private val builderState: ColumnBuilderState<T>,
    private val parent: ColumnsBuilder<T>,
) : FluentTableBuilderApi<T>() {

    fun index(index: Int) = apply {
        builderState.index = index
    }

    fun columnType(columnType: CellType?) = apply {
        builderState.columnType = columnType
    }

    fun column(id: String): ColumnBuilder<T> {
        return parent.column(id)
    }

    fun column(ref: JFunction<T, Any?>): ColumnBuilder<T> {
        return parent.column(ref)
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
) : FluentTableBuilderApi<T>() {

    fun row() = RowBuilder(parent.builderState.rowsBuilderState.addRowBuilder {}, this)

    fun row(at: Int) =
        RowBuilder(parent.builderState.rowsBuilderState.addRowBuilder {
            it.qualifier = RowQualifier(index = RowIndexPredicateLiteral(eq(at)))
        }, this)

    fun row(at: Int, offset: DefaultSteps) =
        RowBuilder(parent.builderState.rowsBuilderState.addRowBuilder {
            it.qualifier = RowQualifier(index = RowIndexPredicateLiteral(eq(at, offset.name)))
        }, this)

    @JvmSynthetic
    override fun up(): TableBuilder<T> = parent

}

class RowBuilder<T> internal constructor(
    private val builderState: RowBuilderState<T>,
    private val parent: RowsBuilder<T>,
) : FluentTableBuilderApi<T>() {

    fun allMatching(predicate: RowPredicate<T>) = apply {
        builderState.qualifier = RowQualifier(matching = predicate)
    }

    fun cell() = CellBuilder(builderState.cellsBuilderState.addCellBuilder { }, this)

    fun cell(id: String) = CellBuilder(builderState.cellsBuilderState.addCellBuilder(id) {}, this)

    fun cell(ref: JFunction<T, Any?>) = CellBuilder(builderState.cellsBuilderState.addCellBuilder(ref.id()) {}, this)

    fun cell(index: Int): CellBuilder<T> = CellBuilder(builderState.cellsBuilderState.addCellBuilder(index) {}, this)

    fun row() = parent.row()

    fun row(at: Int) = parent.row(at)


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
) : FluentTableBuilderApi<T>() {

    fun value(value: Any?) = apply {
        builderState.value = value
    }

    fun eval(expression: RowCellExpression<T>?) = apply {
        builderState.expression = expression
    }

    fun type(type: CellType?) = apply {
        builderState.type = type
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

    fun cell(id: String) = parent.cell(id)

    fun cell(ref: JFunction<T, Any?>) = parent.cell(ref)

    fun cell(index: Int): CellBuilder<T> = parent.cell(index)

    fun row() = parent.row()

    fun row(at: Int) = parent.row(at)

    @JvmSynthetic
    override fun up(): RowBuilder<T> = parent
}
