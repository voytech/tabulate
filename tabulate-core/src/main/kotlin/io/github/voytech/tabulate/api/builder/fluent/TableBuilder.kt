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
import io.github.voytech.tabulate.api.builder.CellBuilder as CellBuilderState
import io.github.voytech.tabulate.api.builder.ColumnBuilder as ColumnBuilderState
import io.github.voytech.tabulate.api.builder.RowBuilder as RowBuilderState
import io.github.voytech.tabulate.api.builder.TableBuilder as TableBuilderState
import java.util.function.Function as JFunction

interface TopLevelBuilder<T>

interface MidLevelBuilder<T, E : TopLevelBuilder<T>> : TopLevelBuilder<T> {
    @JvmSynthetic
    fun up(): E
}

class TableBuilder<T>(internal val builderState: TableBuilderState<T>) : TopLevelBuilder<T>, Builder<Table<T>>() {

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

    fun <A: Attribute<A>,B: AttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    fun <A: Attribute<A>,B: AttributeBuilder<A>> attribute(attributeProvider: Callable<B>, attributeConfigurer: Consumer<B>) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    public override fun build(): Table<T> = builderState.build()
}

class ColumnsBuilder<T>(private val parent: TableBuilder<T>) : MidLevelBuilder<T, TableBuilder<T>>, Builder<Table<T>>() {

    fun column(id: String) = ColumnBuilder(parent.builderState.columnsBuilder.addColumnBuilder(id) {
        it.id = ColumnKey(id = id)
    }, this)

    fun column(ref: JFunction<T, Any?>) = ColumnBuilder(parent.builderState.columnsBuilder.addColumnBuilder(ref.id()) {
        it.id = ColumnKey(ref = ref.id())
    }, this)

    override fun up(): TableBuilder<T> = parent

    public override fun build(): Table<T> = parent.build()

}

class ColumnBuilder<T>(private val builderState: ColumnBuilderState<T>, private val parent: ColumnsBuilder<T>) :
    MidLevelBuilder<T, ColumnsBuilder<T>>, Builder<Table<T>>() {

    fun id(id: ColumnKey<T>) = apply {
        builderState.id = id
    }

    fun index(index: Int?) = apply {
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
    fun <A: ColumnAttribute<A>,B: ColumnAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    @JvmName("columnAttribute")
    fun <A: ColumnAttribute<A>,B: ColumnAttributeBuilder<A>> attribute(attributeProvider: Callable<B>, attributeConfigurer: Consumer<B>) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmName("cellAttribute")
    fun <A: CellAttribute<A>,B: CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    @JvmName("cellAttribute")
    fun <A: CellAttribute<A>,B: CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>, attributeConfigurer: Consumer<B>) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmSynthetic
    override fun up(): ColumnsBuilder<T> = parent

    public override  fun build(): Table<T> = parent.build()

}

class RowsBuilder<T>(private val parent: TableBuilder<T>) : MidLevelBuilder<T, TableBuilder<T>>, Builder<Table<T>>() {

    fun row() = RowBuilder(parent.builderState.rowsBuilder.addRowBuilder {}, this)

    fun row(at: Int) =
        RowBuilder(parent.builderState.rowsBuilder.addRowBuilder {
            it.qualifier = RowQualifier(createAt = RowIndexDef(at))
        }, this)

    fun row(at: Int, offset: DefaultSteps) =
        RowBuilder(parent.builderState.rowsBuilder.addRowBuilder {
            it.qualifier = RowQualifier(createAt = RowIndexDef(at, offset.name))
        }, this)

    @JvmSynthetic
    override fun up(): TableBuilder<T> = parent

    public override  fun build(): Table<T> = parent.build()
}

class RowBuilder<T>(private val builderState: RowBuilderState<T>, private val parent: RowsBuilder<T>) :
    MidLevelBuilder<T, RowsBuilder<T>>, Builder<Table<T>>() {

    fun allMatching(predicate: RowPredicate<T>) = apply {
        builderState.qualifier = RowQualifier(applyWhen = predicate)
    }

    fun insertWhen(predicate: RowPredicate<T>) = apply {
        builderState.qualifier = RowQualifier(createWhen = predicate)
    }

    fun cell() = CellBuilder(builderState.cellsBuilder.addCellBuilder { }, this)

    fun cell(id: String) = CellBuilder(builderState.cellsBuilder.addCellBuilder(id) {}, this)

    fun cell(ref: JFunction<T, Any?>) = CellBuilder(builderState.cellsBuilder.addCellBuilder(ref.id()) {}, this)

    fun cell(index: Int): CellBuilder<T> = CellBuilder(builderState.cellsBuilder.addCellBuilder(index) {}, this)

    fun row() = parent.row()

    fun row(at: Int) = parent.row(at)


    @JvmName("rowAttribute")
    fun <A: RowAttribute<A>,B: RowAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    @JvmName("rowAttribute")
    fun <A: RowAttribute<A>,B: RowAttributeBuilder<A>> attribute(attributeProvider: Callable<B>, attributeConfigurer: Consumer<B>) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }

    @JvmName("cellAttribute")
    fun <A: CellAttribute<A>,B: CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        this.builderState.attribute(attributeProvider.call())
    }

    @JvmName("cellAttribute")
    fun <A: CellAttribute<A>,B: CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>, attributeConfigurer: Consumer<B>) = apply {
        this.builderState.attribute(
            attributeProvider.call().apply {
                attributeConfigurer.accept(this)
            }
        )
    }


    @JvmSynthetic
    override fun up(): RowsBuilder<T> = parent

    public override  fun build(): Table<T> = parent.build()

}

class CellBuilder<T>(private val builderState: CellBuilderState<T>, private val parent: RowBuilder<T>) :
    MidLevelBuilder<T, RowBuilder<T>>, Builder<Table<T>>() {

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
    fun <A: CellAttribute<A>,B: CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>) = apply {
        builderState.attribute(attributeProvider.call())
    }

    @JvmName("cellAttribute")
    fun <A: CellAttribute<A>,B: CellAttributeBuilder<A>> attribute(attributeProvider: Callable<B>, attributeConfigurer: Consumer<B>) = apply {
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

    public override fun build(): Table<T> = parent.build()
}
