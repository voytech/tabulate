package pl.voytech.exporter.testutils

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.template.CellValue
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI
import java.io.File

data class CellPosition(
    val rowIndex: Int,
    val columnIndex: Int
)

interface StateProvider<E>  {
    fun createState(file: File): DelegateAPI<E>
    fun getPresentTableNames(api: DelegateAPI<E>) : List<String>?
    fun hasTableNamed(api: DelegateAPI<E>, name: String): Boolean
}

interface ExtensionResolver<E> {
    fun resolve(api: DelegateAPI<E>, coordinates: Coordinates): CellExtension
}

interface ValueResolver<E> {
    fun resolve(api : DelegateAPI<E>, coordinates: Coordinates): CellValue
}

interface CellTest<E> {
    fun performCellTest(api: DelegateAPI<E>, coordinates: Coordinates, def: CellDefinition? = null)
}


data class CellDefinition(
    val cellExtensions : Set<CellExtension>?,
    val cellValue: CellValue?
)

class TableAssert<T,E> (
    private val tableName: String,
    private val stateProvider: StateProvider<E>,
    private val cellExtensionResolvers: List<ExtensionResolver<E>>? = emptyList(),
    private val cellValueResolver: ValueResolver<E>? = null,
    private val cellTests: Map<CellPosition, CellTest<E>>,
    private val file: File
) {
    lateinit var state: DelegateAPI<E>

    fun perform(): TableAssert<T, E> {
        state = stateProvider.createState(file)
        cellTests.keys.map { Coordinates(tableName, it.rowIndex, it.columnIndex) }.forEach { it ->
            val key = CellPosition(it.rowIndex, it.columnIndex)
            if (cellExtensionResolvers?.isEmpty() == true && cellValueResolver == null) {
                cellTests[key]?.performCellTest(api = state, coordinates = it)
            } else {
                cellTests[key]?.performCellTest(
                    api = state,
                    coordinates = it,
                    def = CellDefinition(
                        cellExtensions = cellExtensionResolvers?.map { resolver -> resolver.resolve(state,it) }?.toSet(),
                        cellValue = cellValueResolver?.resolve(state,it)
                    )
                )
            }
        }
        return this
    }

    fun hasTableName(name: String): Boolean {
        return stateProvider.hasTableNamed(state,  name)
    }

    fun cleanup() {
        file.delete()
    }

}
