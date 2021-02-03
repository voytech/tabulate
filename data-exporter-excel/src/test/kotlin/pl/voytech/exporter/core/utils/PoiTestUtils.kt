package pl.voytech.exporter.core.utils

import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.util.CellRangeAddress
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.context.Coordinates
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import pl.voytech.exporter.testutils.*
import java.io.File
import org.apache.poi.ss.usermodel.CellType as PoiCellType

class PoiStateProvider : StateProvider<ApachePoiExcelFacade> {

    override fun createState(file: File): ApachePoiExcelFacade {
        ZipSecureFile.setMinInflateRatio(0.001)
        return ApachePoiExcelFacade(file.inputStream())
    }

    override fun getPresentTableNames(api: ApachePoiExcelFacade): List<String>? =
        api.workbook().let { (0 until it.numberOfSheets).map { index -> api.workbook().getSheetAt(index).sheetName } }


    override fun hasTableNamed(api: ApachePoiExcelFacade, name: String): Boolean =
        api.workbook().getSheet(name) != null

}

class PoiTableAssert<T>(
    tableName: String,
    cellTests: Map<CellSelect, CellTest<ApachePoiExcelFacade>>,
    file: File
) {
    private val assert = TableAssert<T, ApachePoiExcelFacade>(
        stateProvider = PoiStateProvider(),
        cellAttributeResolvers = listOf(
            PoiCellFontAttributeResolver(),
            PoiCellBackgroundAttributeResolver(),
            PoiCellBordersAttributeResolver(),
            PoiCellAlignmentAttributeResolver(),
            PoiCellDataFormatAttributeResolver()
        ),
        cellValueResolver = object : ValueResolver<ApachePoiExcelFacade> {
            override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellValue {
                val address: CellRangeAddress? =
                    api.workbook().getSheet(coordinates.tableName).mergedRegions.filter { region ->
                        region.containsColumn(coordinates.columnIndex) && region.containsRow(coordinates.rowIndex)
                    }.let { if (it.isNotEmpty()) it[0] else null }
                val colSpan = address?.let { (it.lastColumn - it.firstColumn) + 1 } ?: 1
                val rowSpan = address?.let { (it.lastRow - it.firstRow) + 1 } ?: 1

                api.xssfCell(coordinates)
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

    fun perform(): TableAssert<T, ApachePoiExcelFacade> = assert.perform()
}





