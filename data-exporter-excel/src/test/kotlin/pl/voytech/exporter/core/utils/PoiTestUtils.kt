package pl.voytech.exporter.core.utils

import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.context.Coordinates
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade.workbook
import pl.voytech.exporter.testutils.*
import java.io.File
import org.apache.poi.ss.usermodel.CellType as PoiCellType

class PoiStateProvider : StateProvider<SXSSFWorkbook> {

    override fun createState(file: File): SXSSFWorkbook {
        ZipSecureFile.setMinInflateRatio(0.001)
        return SXSSFWorkbook(WorkbookFactory.create(file) as XSSFWorkbook, 100)
    }

    override fun getPresentTableNames(api: SXSSFWorkbook): List<String>? =
        workbook(api).let { (0 until it.numberOfSheets).map { index -> workbook(api).getSheetAt(index).sheetName } }


    override fun hasTableNamed(api: SXSSFWorkbook, name: String): Boolean =
        workbook(api).getSheet(name) != null

}

class PoiTableAssert<T>(
    tableName: String,
    cellTests: Map<CellSelect, CellTest<SXSSFWorkbook>>,
    file: File
) {
    private val assert = TableAssert<T, SXSSFWorkbook>(
        stateProvider = PoiStateProvider(),
        cellAttributeResolvers = listOf(
            PoiCellFontAttributeResolver(),
            PoiCellBackgroundAttributeResolver(),
            PoiCellBordersAttributeResolver(),
            PoiCellAlignmentAttributeResolver(),
            PoiCellDataFormatAttributeResolver()
        ),
        cellValueResolver = object : ValueResolver<SXSSFWorkbook> {
            override fun resolve(api: SXSSFWorkbook, coordinates: Coordinates): CellValue {
                val address: CellRangeAddress? =
                    workbook(api).getSheet(coordinates.tableName).mergedRegions.filter { region ->
                        region.containsColumn(coordinates.columnIndex) && region.containsRow(coordinates.rowIndex)
                    }.let { if (it.isNotEmpty()) it[0] else null }
                val colSpan = address?.let { (it.lastColumn - it.firstColumn) + 1 } ?: 1
                val rowSpan = address?.let { (it.lastRow - it.firstRow) + 1 } ?: 1

                ApachePoiExcelFacade.xssfCell(api, coordinates)
                    .let {
                        return when (it?.cellType) {
                            PoiCellType.STRING -> CellValue(
                                value = it.stringCellValue,
                                type = CellType.STRING,
                                colSpan = colSpan,
                                rowSpan = rowSpan
                            )
                            PoiCellType.BOOLEAN -> CellValue(
                                value = it.booleanCellValue,
                                type = CellType.BOOLEAN,
                                colSpan = colSpan,
                                rowSpan = rowSpan
                            )
                            PoiCellType.FORMULA -> CellValue(
                                value = it.cellFormula,
                                type = CellType.NATIVE_FORMULA,
                                colSpan = colSpan,
                                rowSpan = rowSpan
                            )
                            PoiCellType.NUMERIC -> CellValue(
                                value = it.numericCellValue,
                                type = CellType.NUMERIC,
                                colSpan = colSpan,
                                rowSpan = rowSpan
                            )
                            else -> CellValue(value = "null", type = null, colSpan = colSpan, rowSpan = rowSpan)
                        }
                    }

            }
        },
        cellTests = cellTests,
        file = file,
        tableName = tableName
    )

    fun perform(): TableAssert<T, SXSSFWorkbook> = assert.perform()
}



