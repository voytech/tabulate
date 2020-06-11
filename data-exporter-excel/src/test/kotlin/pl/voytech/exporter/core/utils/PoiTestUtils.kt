package pl.voytech.exporter.core.utils

import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.CellType as PoiCellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.CellFontExtension
import pl.voytech.exporter.core.model.extension.style.Color
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle
import pl.voytech.exporter.core.template.CellValue
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI
import pl.voytech.exporter.core.testutils.*
import pl.voytech.exporter.impl.template.excel.PoiUtils.pixelsFromHeight
import pl.voytech.exporter.impl.template.excel.PoiWrapper
import pl.voytech.exporter.impl.template.excel.PoiWrapper.workbook
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PoiStateProvider : StateProvider<SXSSFWorkbook> {

    override fun createState(file: File): DelegateAPI<SXSSFWorkbook> {
        ZipSecureFile.setMinInflateRatio(0.001)
        return DelegateAPI(SXSSFWorkbook(WorkbookFactory.create(file) as XSSFWorkbook, 100))
    }

    override fun getPresentTableNames(api: DelegateAPI<SXSSFWorkbook>): List<String>? =
        workbook(api).let { (0 until it.numberOfSheets).map { index -> workbook(api).getSheetAt(index).sheetName } }


    override fun hasTableNamed(api: DelegateAPI<SXSSFWorkbook>, name: String): Boolean = workbook(api).getSheet(name) != null

}

//@TODO move to core.testutils.cellassertions
class AssertCellValue(private val expectedValue: Any, private val expectedType: CellType? = null) : CellTest<SXSSFWorkbook> {
    override fun performCellTest(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, def: CellDefinition?) {
        assertNotNull(def?.cellValue,"Expected cell value to be present")
        assertEquals(expectedValue, def?.cellValue?.value, "Expected cell value to be $expectedValue")
        expectedType?.let {
            assertNotNull(def?.cellValue?.type,"Expected cell type to be present")
            assertEquals(expectedType, def?.cellValue?.type, "Expected cell type to be $it")
        }
    }
}
//@TODO move to core.testutils.cellassertions
class AssertMany(private val cellTests: List<CellTest<SXSSFWorkbook>>) : CellTest<SXSSFWorkbook> {
    override fun performCellTest(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, def: CellDefinition?) {
        cellTests.forEach { it.performCellTest(api,coordinates,def) }
    }
}

//@TODO move to core.testutils.cellassertions
class AssertEqualExtension<T : CellExtension>(private val expectedExtension: T) : CellTest<SXSSFWorkbook> {
    override fun performCellTest(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, def: CellDefinition?) {
        def?.cellExtensions
            ?.filter { it::class == expectedExtension::class }
            ?.forEach { assertEquals(expectedExtension,it,"expected extension to equal $expectedExtension") }
    }
}

//@TODO move to core.testutils.cellassertions
class AssertExtensionExpression<T: CellExtension>(private val extensionClass: KClass<T>, private val invoke: (T) -> Unit) : CellTest<SXSSFWorkbook> {
    @Suppress("UNCHECKED_CAST")
    override fun performCellTest(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, def: CellDefinition?) {
        def?.cellExtensions
            ?.filter { it::class == extensionClass }
            ?.forEach { invoke.invoke(it as T) }
    }
}


class PoiTableAssert<T>(
    tableName: String,
    cellTests: Map<CellPosition, CellTest<SXSSFWorkbook>>,
    file: File
) {
    private val assert = TableAssert<T, SXSSFWorkbook>(
        stateProvider = PoiStateProvider(),
        cellExtensionResolvers = listOf(PoiCellFontExtensionResolver()),
        cellValueResolver = object : ValueResolver<SXSSFWorkbook> {
            override fun resolve(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellValue {
                PoiWrapper.xssfCell(api,coordinates)
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



