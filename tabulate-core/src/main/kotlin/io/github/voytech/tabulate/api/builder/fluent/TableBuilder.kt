package io.github.voytech.tabulate.api.builder.fluent

import io.github.voytech.tabulate.api.builder.Builder
import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.api.builder.CellBuilder as BaseCellBuilder
import io.github.voytech.tabulate.api.builder.ColumnBuilder as BaseColumnBuilder
import io.github.voytech.tabulate.api.builder.RowBuilder as BaseRowBuilder
import io.github.voytech.tabulate.api.builder.TableBuilder as BaseTableBuilder


interface TopLevelBuilder<T>

interface MidLevelBuilder<T, E : TopLevelBuilder<T>> : TopLevelBuilder<T> {
    @JvmSynthetic
    fun out(): E

}

class TableBuilder<T>(internal val builderBase: BaseTableBuilder<T>) : TopLevelBuilder<T>, Builder<Table<T>> {

    fun name(name: String?) = apply {
        this.builderBase.name = name
    }

    fun firstRow(firstRow: Int?) = apply {
        this.builderBase.firstRow = firstRow
    }

    fun firstColumn(firstColumn: Int?) = apply {
        this.builderBase.firstColumn = firstColumn
    }

    fun columns() = ColumnsBuilder(this)

    fun rows() = RowsBuilder(this)

    fun attribute(vararg attribute: Attribute<*>) = apply {
        this.builderBase.attributes(*attribute)
    }

    override fun build(): Table<T> = builderBase.build()
}

class ColumnsBuilder<T>(private val parent: TableBuilder<T>) : MidLevelBuilder<T, TableBuilder<T>>, Builder<Table<T>> {

    fun column(id: String) = ColumnBuilder(parent.builderBase.columnsBuilder.addColumnBuilder(id) {
        it.id = ColumnKey(id = id)
    }, this)

    fun column(ref: ((record: T) -> Any?)) = ColumnBuilder(parent.builderBase.columnsBuilder.addColumnBuilder(ref) {
        it.id = ColumnKey(ref = ref)
    }, this)

    override fun out(): TableBuilder<T> = parent

    override fun build(): Table<T> = parent.build()

}

class ColumnBuilder<T>(private val builderBase: BaseColumnBuilder<T>, private val parent: ColumnsBuilder<T>) :
    MidLevelBuilder<T, ColumnsBuilder<T>>, Builder<Table<T>> {

    fun id(id: ColumnKey<T>) = apply {
        builderBase.id = id
    }

    fun dataFormatter(dataFormatter: ((field: Any) -> Any)?) = apply {
        builderBase.dataFormatter = dataFormatter
    }

    fun index(index: Int?) = apply {
        builderBase.index = index
    }

    fun columnType(columnType: CellType?) = apply {
        builderBase.columnType = columnType
    }

    fun column(id: String): ColumnBuilder<T> {
        return parent.column(id)
    }

    fun column(ref: ((record: T) -> Any?)): ColumnBuilder<T> {
        return parent.column(ref)
    }

    fun rows() = out().out().rows()

    fun attribute(vararg attribute: Attribute<*>) = apply {
        builderBase.attributes(*attribute)
    }

    @JvmSynthetic
    override fun out(): ColumnsBuilder<T> = parent

    override fun build(): Table<T> = parent.build()

}

class RowsBuilder<T>(private val parent: TableBuilder<T>) : MidLevelBuilder<T, TableBuilder<T>>, Builder<Table<T>> {

    fun row() = RowBuilder(parent.builderBase.rowsBuilder.addRowBuilder {}, this)

    fun row(at: Int) =
        RowBuilder(parent.builderBase.rowsBuilder.addRowBuilder { it.qualifier = RowQualifier(createAt = at) }, this)

    @JvmSynthetic
    override fun out(): TableBuilder<T> = parent

    override fun build(): Table<T> = parent.build()
}

class RowBuilder<T>(private val builderBase: BaseRowBuilder<T>, private val parent: RowsBuilder<T>) :
    MidLevelBuilder<T, RowsBuilder<T>>, Builder<Table<T>> {

    fun allMatching(predicate: RowPredicate<T>) = apply {
        builderBase.qualifier = RowQualifier(applyWhen = predicate)
    }

    fun insertWhen(predicate: RowPredicate<T>) = apply {
        builderBase.qualifier = RowQualifier(createWhen = predicate)
    }

    fun cell() = CellBuilder(builderBase.cellsBuilder.addCellBuilder { }, this)

    fun forColumn(id: String) = CellBuilder(builderBase.cellsBuilder.addCellBuilder(id) {}, this)

    fun forColumn(ref: ((record: T) -> Any?)) = CellBuilder(builderBase.cellsBuilder.addCellBuilder(ref) {}, this)

    fun cell(index: Int): CellBuilder<T> = CellBuilder(builderBase.cellsBuilder.addCellBuilder(index) {}, this)

    fun row() = parent.row()

    fun row(at: Int) = parent.row(at)

    fun attribute(vararg attribute: Attribute<*>) = apply {
        builderBase.attributes(*attribute)
    }

    @JvmSynthetic
    override fun out(): RowsBuilder<T> = parent

    override fun build(): Table<T> = parent.build()

}

class CellBuilder<T>(private val builderBase: BaseCellBuilder<T>, private val parent: RowBuilder<T>) :
    MidLevelBuilder<T, RowBuilder<T>>, Builder<Table<T>> {

    fun value(value: Any?) = apply {
        builderBase.value = value
    }

    fun eval(expression: RowCellExpression<T>?) = apply {
        builderBase.expression = expression
    }

    fun type(type: CellType?) = apply {
        builderBase.type = type
    }

    fun colSpan(colSpan: Int) = apply {
        builderBase.colSpan = colSpan
    }

    fun rowSpan(rowSpan: Int) = apply {
        builderBase.rowSpan = rowSpan
    }

    fun forColumn(id: String) = parent.forColumn(id)

    fun forColumn(ref: ((record: T) -> Any?)) = parent.forColumn(ref)

    fun cell(index: Int): CellBuilder<T> = parent.cell(index)

    fun row() = parent.row()

    fun row(at: Int) = parent.row(at)

    @JvmSynthetic
    override fun out(): RowBuilder<T> = parent

    override fun build(): Table<T> = parent.build()
}
