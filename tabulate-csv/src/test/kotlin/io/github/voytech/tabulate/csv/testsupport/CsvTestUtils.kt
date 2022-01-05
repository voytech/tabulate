package io.github.voytech.tabulate.csv.testsupport

import io.github.voytech.tabulate.csv.CsvRenderingContext
import io.github.voytech.tabulate.template.operations.CellValue
import io.github.voytech.tabulate.template.operations.Coordinates
import io.github.voytech.tabulate.test.*

import java.io.File

class CsvReadRenderingContext(file: File): CsvRenderingContext() {
    val lines: List<String> = file.readLines()
}

class CsvStateProvider : StateProvider<CsvReadRenderingContext> {

    override fun createState(file: File): CsvReadRenderingContext = CsvReadRenderingContext(file)
}

class CsvTableAssert<T>(
    cellTests: Map<CellSelect, CellTest>,
    file: File,
    separator: String = ","
) {
    private val assert = TableAssert<T, CsvReadRenderingContext>(
            stateProvider = CsvStateProvider(),
            cellAttributeResolvers = listOf(),
            cellValueResolver = object : ValueResolver<CsvReadRenderingContext> {

                override fun resolve(api: CsvReadRenderingContext, coordinates: Coordinates): CellValue {
                    val line = api.lines[coordinates.rowIndex]
                    val cells = line.split(separator)
                    return CellValue(cells[coordinates.columnIndex])
                }
            },
            cellTests = cellTests,
            file = file,
            tableName = "omitted"
    )

    fun perform(): TableAssert<T,CsvReadRenderingContext> = assert.perform()
}





