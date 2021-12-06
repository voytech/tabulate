package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.excel.template.ApachePoiRenderingContext
import io.github.voytech.tabulate.template.operations.CellValue
import io.github.voytech.tabulate.template.operations.Coordinates
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import org.apache.poi.ss.usermodel.CellType as PoiCellType

class PoiStateProvider : StateProvider<ApachePoiRenderingContext> {

    override fun createState(file: File): ApachePoiRenderingContext {
        ZipSecureFile.setMinInflateRatio(0.001)
        return ApachePoiRenderingContext().also {
            it.createWorkbook(file.inputStream())
        }
    }

    override fun getPresentTableNames(api: ApachePoiRenderingContext): List<String> =
            api.workbook().let { (0 until it.numberOfSheets).map { index -> api.workbook().getSheetAt(index).sheetName } }


    override fun hasTableNamed(api: ApachePoiRenderingContext, name: String): Boolean =
            api.workbook().getSheet(name) != null

}

class PoiTableAssert<T>(
        tableName: String,
        cellTests: Map<CellSelect, CellTest<ApachePoiRenderingContext>>,
        file: File
) {
    private val assert = TableAssert<T, ApachePoiRenderingContext>(
            stateProvider = PoiStateProvider(),
            cellAttributeResolvers = listOf(
                    PoiCellFontAttributeResolver(),
                    PoiCellBackgroundAttributeResolver(),
                    PoiCellBordersAttributeResolver(),
                    PoiCellAlignmentAttributeResolver(),
                    PoiCellDataFormatAttributeResolver(),
                    PoiCellTypeHintAttributeResolver(),
            ),
            cellValueResolver = object : ValueResolver<ApachePoiRenderingContext> {
                override fun resolve(api: ApachePoiRenderingContext, coordinates: Coordinates): CellValue {
                    val address: CellRangeAddress? =
                            api.workbook().getSheet(coordinates.tableName).mergedRegions.filter { region ->
                                region.containsColumn(coordinates.columnIndex) && region.containsRow(coordinates.rowIndex)
                            }.let { if (it.isNotEmpty()) it[0] else null }
                    val colSpan = address?.let { (it.lastColumn - it.firstColumn) + 1 } ?: 1
                    val rowSpan = address?.let { (it.lastRow - it.firstRow) + 1 } ?: 1
                    return api.getImageAsCellValue(coordinates) ?: api.xssfCell(coordinates)
                            .let {
                                return when (it?.cellType) {
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
                }
            },
            cellTests = cellTests,
            file = file,
            tableName = tableName
    )

    fun perform(): TableAssert<T, ApachePoiRenderingContext> = assert.perform()
}





