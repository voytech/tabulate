package io.github.voytech.tabulate.test

import io.github.voytech.tabulate.components.table.operation.CellValue
import io.github.voytech.tabulate.components.table.operation.Coordinates
import io.github.voytech.tabulate.core.model.Attribute
import java.io.File

sealed interface Select<CAT: Attribute<*>>
sealed interface SelectRange<CAT: Attribute<*>, E: Select<CAT>> : Select<CAT> {
    fun  onSelect(select: (E) -> Unit)
}
sealed interface SelectAll<CAT: Attribute<*>>: Select<CAT>

data class ColumnPosition(val columnIndex: Int) : Select<Attribute<*>>
data class ColumnRange(val columnIndices: IntRange) : SelectRange<Attribute<*>, ColumnPosition> {
    override fun onSelect(select: (ColumnPosition) -> Unit) {
        columnIndices.forEach { index -> select.invoke(ColumnPosition(index)) }
    }
}

data class RowPosition(val rowIndex: Int) : Select<Attribute<*>>
data class RowRange(val rowIndices: IntRange) : SelectRange<Attribute<*>, RowPosition> {
    override fun onSelect(select: (RowPosition) -> Unit) {
        rowIndices.forEach { index -> select.invoke(RowPosition(index)) }
    }
}

data class CellPosition(val rowIndex: Int, val columnIndex: Int) : Select<Attribute<*>>
data class CellRange(val rowIndices: IntRange, val columnIndices: IntRange) : SelectRange<Attribute<*>, CellPosition> {
    override fun onSelect(select: (CellPosition) -> Unit) {
        rowIndices.forEach { rowIndex ->
            columnIndices.forEach { columnIndex ->
                select.invoke(CellPosition(rowIndex, columnIndex))
            }
        }
    }
}

object EntireTable : SelectAll<Attribute<*>>

fun interface StateProvider<E> {
    fun createState(file: File): E
}

fun interface AttributeResolver<E, CAT: Attribute<*>, S: Select<CAT>> {
    fun resolve(api: E, tableId: String, select: S): CAT?
}

fun interface ValueResolver<E> {
    fun resolve(api: E, coordinates: Coordinates): CellValue
}

fun interface AttributeTest<CAT: Attribute<*>> {
    fun performTest(def: Set<CAT>)
}

fun interface ValueTest {
    fun performTest(value: CellValue?)
}


/**
 * TableAssert test utility.
 * After reading rendering context from test file, performs table assertions against expected attributes and cell values.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
class TableAssert<T, E>(
    private val tableName: String,
    private val stateProvider: StateProvider<E>,
    private val attributeResolvers: Map<Class<out Select<*>>,List<AttributeResolver<E, *, out Select<*>>>>? = emptyMap(),
    private val cellValueResolver: ValueResolver<E>? = null,
    private val attributeTests: Map<Select<*>, AttributeTest<*>> = emptyMap(),
    private val valueTests: Map<CellPosition, ValueTest> = emptyMap(),
    private val file: File
) {
    var state: E? = null

    private fun <CAT: Attribute<*>> performTests(select: Select<CAT>) {
        val attributeSet = attributeResolvers?.filter { select.javaClass.isAssignableFrom(it.key) }
            ?.flatMap { it.value }
            ?.map { resolver ->
            (resolver as? AttributeResolver<E,CAT,Select<CAT>>)?.resolve(state!!, tableName, select)
        }?.toSet()
        (attributeTests[select] as? AttributeTest<CAT>)?.performTest(attributeSet as Set<CAT>)
        if (select is CellPosition) {
            cellValueResolver?.resolve(state!!, Coordinates(tableName, select.rowIndex, select.columnIndex)).run {
                valueTests[select]?.performTest(this)
            }
        }
    }

    fun perform(): TableAssert<T, E> {
        state = stateProvider.createState(file)
        attributeTests.keys.forEach { select ->
            when (select) {
                is SelectRange<*,*> -> select.onSelect { performTests(it) }
                else -> performTests(select)
            }
        }
        return this
    }

    fun cleanup() {
        file.delete()
    }

}

