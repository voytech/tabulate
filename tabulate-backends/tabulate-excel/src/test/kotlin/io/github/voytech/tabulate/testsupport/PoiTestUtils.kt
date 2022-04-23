package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.components.table.operation.CellValue
import io.github.voytech.tabulate.components.table.operation.Coordinates
import io.github.voytech.tabulate.test.*
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import org.apache.poi.ss.usermodel.CellType as PoiCellType

class PoiStateProvider : StateProvider<ApachePoiRenderingContext> {

    override fun createState(file: File): ApachePoiRenderingContext {
        ZipSecureFile.setMinInflateRatio(0.001)
        return ApachePoiRenderingContext().also {
            it.provideWorkbook(file.inputStream())
        }
    }

}

class PoiTableAssert<T>(
    tableName: String,
    valueTests: Map<CellPosition, ValueTest>? = null,
    attributeTests: Map<Select<*>, AttributeTest<*>>? = null,
    file: File
) {
    private val assert = TableAssert<T, ApachePoiRenderingContext>(
        tableName = tableName,
        stateProvider = PoiStateProvider(),
        attributeResolvers = mapOf(
            CellPosition::class.java to listOf(
                PoiCellFontAttributeResolver(),
                PoiCellBackgroundAttributeResolver(),
                PoiCellBordersAttributeResolver(),
                PoiCellAlignmentAttributeResolver(),
                PoiCellTypeHintAttributeResolver(),
                PoiCellCommentAttributeResolver(),
                PoiCellDataFormatAttributeResolver()
            ),
            ColumnPosition::class.java to listOf(PoiColumnWidthAttributeResolver()),
            RowPosition::class.java to listOf(PoiRowHeightAttributeResolver()),
            EntireTable::class.java to listOf(PoiPrintingAttributeResolver())
        ),
        cellValueResolver = { api: ApachePoiRenderingContext, coordinates: Coordinates ->
            val address: CellRangeAddress? =
                api.workbook().getSheet(coordinates.tableName).mergedRegions.filter { region ->
                    region.containsColumn(coordinates.columnIndex) && region.containsRow(coordinates.rowIndex)
                }.let { if (it.isNotEmpty()) it[0] else null }
            val colSpan = address?.let { it.lastColumn - it.firstColumn + 1 } ?: 1
            val rowSpan = address?.let { it.lastRow - it.firstRow + 1 } ?: 1
            api.getImageAsCellValue(coordinates) ?: api.xssfCell(coordinates)
                .let {
                    when (it?.cellType) {
                        PoiCellType.STRING -> CellValue(
                            value = it.stringCellValue,
                            colSpan = colSpan,
                            rowSpan = rowSpan
                        )
                        PoiCellType.BOOLEAN -> CellValue(
                            value = it.booleanCellValue,
                            colSpan = colSpan,
                            rowSpan = rowSpan
                        )
                        PoiCellType.FORMULA -> CellValue(
                            value = it.cellFormula,
                            colSpan = colSpan,
                            rowSpan = rowSpan
                        )
                        PoiCellType.NUMERIC -> CellValue(
                            value = it.numericCellValue,
                            colSpan = colSpan,
                            rowSpan = rowSpan
                        )
                        else -> CellValue(value = "null", colSpan = colSpan, rowSpan = rowSpan)
                    }
                }

        },
        attributeTests = attributeTests ?: emptyMap(),
        valueTests = valueTests ?: emptyMap(),
        file = file
    )

    fun perform(): TableAssert<T, ApachePoiRenderingContext> = assert.perform()
}





