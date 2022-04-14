package io.github.voytech.tabulate.test

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.template.operations.CellValue
import io.github.voytech.tabulate.template.operations.Coordinates
import java.io.File

interface Select<CAT: Attribute<*>>
interface SelectRange<CAT: Attribute<*>, E: Select<CAT>> : Select<CAT> {
    fun  onSelect(select: (E) -> Unit)
}
interface SelectAll<CAT: Attribute<*>>: Select<CAT>

data class ColumnPosition(val columnIndex: Int) : Select<ColumnAttribute<*>>
data class ColumnRange(val columnIndices: IntRange) : SelectRange<ColumnAttribute<*>, ColumnPosition> {
    override fun onSelect(select: (ColumnPosition) -> Unit) {
        columnIndices.forEach { index -> select.invoke(ColumnPosition(index)) }
    }
}

data class RowPosition(val rowIndex: Int) : Select<RowAttribute<*>>
data class RowRange(val rowIndices: IntRange) : SelectRange<RowAttribute<*>, RowPosition> {
    override fun onSelect(select: (RowPosition) -> Unit) {
        rowIndices.forEach { index -> select.invoke(RowPosition(index)) }
    }
}

data class CellPosition(val rowIndex: Int, val columnIndex: Int) : Select<CellAttribute<*>>
data class CellRange(val rowIndices: IntRange, val columnIndices: IntRange) : SelectRange<CellAttribute<*>, CellPosition> {
    override fun onSelect(select: (CellPosition) -> Unit) {
        rowIndices.forEach { rowIndex ->
            columnIndices.forEach { columnIndex ->
                select.invoke(CellPosition(rowIndex, columnIndex))
            }
        }
    }
}

class EntireTable : SelectAll<TableAttribute<*>>

interface StateProvider<E> {
    fun createState(file: File): E
}

interface AttributeResolver<E, CAT: Attribute<*>, S: Select<CAT>> {
    fun resolve(api: E, tableId: String, select: S): CAT
}

interface ValueResolver<E> {
    fun resolve(api: E, coordinates: Coordinates): CellValue
}

interface AttributeTest<CAT: Attribute<*>> {
    fun performTest(sheetName: String, def: Set<CAT>? = null)
}

data class CellDefinition(
    val cellAttributes: Set<CellAttribute<*>>?,
    val cellValue: CellValue?
)

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
    private val tests: Map<Select<*>, AttributeTest<*>> = emptyMap(),
    private val file: File
) {
    var state: E? = null

    private fun <CAT: Attribute<*>> performTests(select: Select<CAT>) {
        val attributeSet = attributeResolvers?.filter { select.javaClass.isAssignableFrom(it.key) }
            ?.flatMap { it.value }
            ?.map { resolver ->
            (resolver as? AttributeResolver<E,CAT,Select<CAT>>)?.resolve(state!!, tableName, select)
        }?.toSet()
        (tests[select] as? AttributeTest<CAT>)?.performTest(tableName, attributeSet as Set<CAT>?)
        /*
        if (cellAttributeResolvers?.isEmpty() == true && cellValueResolver == null) {
            cellTests[select]?.performCellTest(coordinates = coordinates)
        } else {
            cellTests[select]?.performCellTest(
                coordinates = coordinates,
                def = CellDefinition(
                    cellAttributes = cellAttributeResolvers?.map { it.resolve(state!!, coordinates) }?.toSet(),
                    cellValue = cellValueResolver?.resolve(state!!, coordinates)
                )
            )
        }*/
    }

    fun perform(): TableAssert<T, E> {
        state = stateProvider.createState(file)
        tests.keys.forEach { select ->
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

