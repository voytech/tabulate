package io.github.voytech.tabulate.csv.testsupport

import io.github.voytech.tabulate.csv.CsvRenderingContext
import io.github.voytech.tabulate.components.table.rendering.CellValue
import io.github.voytech.tabulate.test.CellPosition
import io.github.voytech.tabulate.test.StateProvider
import io.github.voytech.tabulate.test.TableAssert
import io.github.voytech.tabulate.test.ValueTest
import java.io.File

class CsvReadRenderingContext(file: File): CsvRenderingContext() {
    val lines: List<String> = file.readLines()
}

class CsvStateProvider : StateProvider<CsvReadRenderingContext> {

    override fun createState(file: File): CsvReadRenderingContext = CsvReadRenderingContext(file)
}

class CsvTableAssert<T>(
    cellTests: Map<CellPosition, ValueTest>,
    file: File,
    separator: String = ","
) {
    private val assert = TableAssert<T, CsvReadRenderingContext>(
            stateProvider = CsvStateProvider(),
            cellValueResolver = { api, coordinates ->
                val line = api.lines[coordinates.rowIndex]
                val cells = line.split(separator)
                CellValue(cells[coordinates.columnIndex])
            },
            valueTests = cellTests,
            file = file,
            tableName = "omitted"
    )

    fun perform(): TableAssert<T,CsvReadRenderingContext> = assert.perform()
}





