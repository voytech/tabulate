package pl.voytech.exporter.core.utils

import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.template.CellValue
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI
import pl.voytech.exporter.impl.template.excel.PoiWrapper
import pl.voytech.exporter.impl.template.excel.PoiWrapper.workbook
import pl.voytech.exporter.testutils.*
import java.io.File
import org.apache.poi.ss.usermodel.CellType as PoiCellType

class PoiStateProvider : StateProvider<SXSSFWorkbook> {

    override fun createState(file: File): DelegateAPI<SXSSFWorkbook> {
        ZipSecureFile.setMinInflateRatio(0.001)
        return DelegateAPI(SXSSFWorkbook(WorkbookFactory.create(file) as XSSFWorkbook, 100))
    }

    override fun getPresentTableNames(api: DelegateAPI<SXSSFWorkbook>): List<String>? =
        workbook(api).let { (0 until it.numberOfSheets).map { index -> workbook(api).getSheetAt(index).sheetName } }


    override fun hasTableNamed(api: DelegateAPI<SXSSFWorkbook>, name: String): Boolean =
        workbook(api).getSheet(name) != null

}


class PoiTableAssert<T>(
    tableName: String,
    cellTests: Map<CellPosition, CellTest<SXSSFWorkbook>>,
    file: File
) {
    private val assert = TableAssert<T, SXSSFWorkbook>(
        stateProvider = PoiStateProvider(),
        cellExtensionResolvers = listOf(
            PoiCellFontExtensionResolver(),
            PoiCellBackgroundExtensionResolver(),
            PoiCellBordersExtensionResolver(),
            PoiCellAlignmentExtensionResolver(),
            PoiCellDataFormatExtensionResolver()
        ),
        cellValueResolver = object : ValueResolver<SXSSFWorkbook> {
            override fun resolve(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellValue {
                PoiWrapper.xssfCell(api, coordinates)
                    .let {
                        return when (it?.cellType) {
                            PoiCellType.STRING -> CellValue(value = it.stringCellValue, type = CellType.STRING)
                            PoiCellType.BOOLEAN -> CellValue(value = it.booleanCellValue, type = CellType.BOOLEAN)
                            PoiCellType.FORMULA -> CellValue(value = it.cellFormula, type = CellType.NATIVE_FORMULA)
                            PoiCellType.NUMERIC -> CellValue(value = it.numericCellValue, type = CellType.NUMERIC)
                            else -> CellValue(value = "null", type = null)
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



