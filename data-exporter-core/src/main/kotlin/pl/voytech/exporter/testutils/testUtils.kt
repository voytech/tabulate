package pl.voytech.exporter.testutils

import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.context.Coordinates
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
    fun getPresentTableNames(api: E): List<String>?
    fun hasTableNamed(api: E, name: String): Boolean
}

interface AttributeResolver<E> {
    fun resolve(api: E, coordinates: Coordinates): CellAttribute
}

interface ValueResolver<E> {
    fun resolve(api: E, coordinates: Coordinates): CellValue
}

interface CellTest<E> {
    fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition? = null)
}


data class CellDefinition(
    val cellAttributes: Set<CellAttribute>?,
    val cellValue: CellValue?
)

class TableAssert<T, E>(
    private val tableName: String,
    private val stateProvider: StateProvider<E>,
    private val cellAttributeResolvers: List<AttributeResolver<E>>? = emptyList(),
    private val cellValueResolver: ValueResolver<E>? = null,
    private val cellTests: Map<CellSelect, CellTest<E>>,
    private val file: File
) {
    var state: E? = null

    private fun performTestsOnCell(coordinates: Coordinates, select: CellSelect) {
        if (cellAttributeResolvers?.isEmpty() == true && cellValueResolver == null) {
            cellTests[select]?.performCellTest(api = state!!, coordinates = coordinates)
        } else {
            cellTests[select]?.performCellTest(
                api = state!!,
                coordinates = coordinates,
                def = CellDefinition(
                    cellAttributes = cellAttributeResolvers?.map { resolver -> resolver.resolve(state!!, coordinates) }
                        ?.toSet(),
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
