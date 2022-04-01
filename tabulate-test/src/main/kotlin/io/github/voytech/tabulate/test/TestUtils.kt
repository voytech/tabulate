package io.github.voytech.tabulate.test

import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.template.operations.CellValue
import io.github.voytech.tabulate.template.operations.Coordinates
import java.io.File

interface CellSelect

data class CellPosition(
    val rowIndex: Int,
    val columnIndex: Int
) : CellSelect

data class CellRange(
    val rowIndices: IntRange,
    val columnIndices: IntRange
) : CellSelect

interface StateProvider<E> {
    fun createState(file: File): E
}

interface AttributeResolver<E> {
    fun resolve(api: E, coordinates: Coordinates): CellAttribute<*>
}

interface ValueResolver<E> {
    fun resolve(api: E, coordinates: Coordinates): CellValue
}

interface CellTest {
    fun performCellTest(coordinates: Coordinates, def: CellDefinition? = null)
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
class TableAssert<T, E>(
    private val tableName: String,
    private val stateProvider: StateProvider<E>,
    private val cellAttributeResolvers: List<AttributeResolver<E>>? = emptyList(),
    private val cellValueResolver: ValueResolver<E>? = null,
    private val cellTests: Map<CellSelect, CellTest>,
    private val file: File
) {
    var state: E? = null

    private fun performTestsOnCell(coordinates: Coordinates, select: CellSelect) {
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
        }
    }

    fun perform(): TableAssert<T, E> {
        state = stateProvider.createState(file)
        cellTests.keys.forEach { select ->
            when (select) {
                is CellPosition -> performTestsOnCell(
                    select = select,
                    coordinates = Coordinates(tableName, select.rowIndex, select.columnIndex)
                )
                is CellRange -> select.columnIndices.forEach { columnIndex ->
                    select.rowIndices.forEach { rowIndex ->
                        performTestsOnCell(
                            select = select,
                            coordinates = Coordinates(tableName, rowIndex, columnIndex)
                        )
                    }
                }
            }
        }
        return this
    }

    fun cleanup() {
        file.delete()
    }

}
