package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.model.extension.TableExtension
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertCell
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertTableSheet
import pl.voytech.exporter.impl.template.excel.PoiWrapper.workbook
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

internal class StreamingExcelRowTableOperation(rowHints: List<RowExtensionOperation<out RowExtension, SXSSFWorkbook>>) :
    RowOperationWithExtensions<SXSSFWorkbook>(rowHints) {

    override fun renderRow(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates) {
        assertRow(state, coordinates)
    }

}

internal class CreateStreamingExcelDocumentOperation(private val templateFile: InputStream?) :
    CreateDocumentOperation<SXSSFWorkbook> {
    override fun createDocument(): DelegateAPI<SXSSFWorkbook> {
        return DelegateAPI(templateFile?.let { SXSSFWorkbook(WorkbookFactory.create(it) as XSSFWorkbook?, 100) }
            ?: SXSSFWorkbook())
    }
}

internal class CreateStreamingExcelTableOperation<T>(tableExtensionOperations: List<TableExtensionOperation<out TableExtension, SXSSFWorkbook>>) :
    CreateTableOperationWithExtensions<T, SXSSFWorkbook>(tableExtensionOperations) {
    override fun initializeTable(state: DelegateAPI<SXSSFWorkbook>, table: Table<T>): DelegateAPI<SXSSFWorkbook> {
        return assertTableSheet(state, table.name).let { state }
    }
}

internal class FinishStreamingExcelDocumentOperation : FinishDocumentOperations<SXSSFWorkbook> {
    override fun finishDocument(state: DelegateAPI<SXSSFWorkbook>): FileData<ByteArray> {
        val outputStream = ByteArrayOutputStream()
        workbook(state).run {
            write(outputStream)
            close()
        }
        return FileData(content = outputStream.toByteArray())
    }

    override fun finishDocument(state: DelegateAPI<SXSSFWorkbook>, stream: OutputStream) {
        workbook(state).run {
            write(stream)
            close()
        }
    }
}

internal class StreamingExcelHeaderCellOperations(cellExtensions: List<CellExtensionOperation<out CellExtension, SXSSFWorkbook>>) :
    HeaderCellOperationsWithExtensions<SXSSFWorkbook>(cellExtensions) {
    override fun renderHeaderCell(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, columnTitle: String?) {
        assertCell(state, coordinates).let { cell ->
            columnTitle?.let { cell.setCellValue(it) }
        }
    }

}

internal class StreamingExcelRowCellOperations(cellExtensions: List<CellExtensionOperation<out CellExtension, SXSSFWorkbook>>) :
    RowCellOperationsWithExtensions<SXSSFWorkbook>(cellExtensions) {

    private fun toDateValue(value: Any): Date {
        return when (value) {
            is LocalDate -> Date.from(value.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
            is LocalDateTime -> Date.from(value.atZone(ZoneId.systemDefault()).toInstant())
            is Date -> value
            is String -> Date.from(Instant.parse(value))
            else -> throw IllegalStateException("Could not parse Date.")
        }
    }

    private fun setCellValue(cell: SXSSFCell, value: CellValue?) {
        value?.let { v ->
            v.type?.let {
                when (it) {
                    CellType.STRING -> cell.setCellValue(v.value as String)
                    CellType.BOOLEAN -> cell.setCellValue(v.value as Boolean)
                    CellType.DATE -> cell.setCellValue(toDateValue(v.value))
                    CellType.NUMERIC -> cell.setCellValue((v.value as Number).toDouble())
                    CellType.NATIVE_FORMULA -> cell.cellFormula = v.value.toString()
                    CellType.FORMULA -> cell.cellFormula = v.value.toString()
                    CellType.ERROR -> throw IllegalStateException("CellType.ERROR not supported.")
                }
            } ?: v.run {
                when (this.value) {
                    is String -> cell.setCellValue(this.value as String)
                    is Boolean -> cell.setCellValue(this.value as Boolean)
                    is LocalDate -> cell.setCellValue(toDateValue(this.value))
                    is LocalDateTime -> cell.setCellValue(toDateValue(this.value))
                    is Date -> cell.setCellValue(this.value as Date)
                    is Number -> cell.setCellValue((this.value as Number).toDouble())
                }
            }
        }
    }

    override fun renderRowCell(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, value: CellValue?) {
        assertCell(state, coordinates).also { setCellValue(it, value) }
    }

}

fun <T> xlsxExport(templateFile: InputStream? = null) = ExportOperations(
    createDocumentOperation = CreateStreamingExcelDocumentOperation(templateFile),
    createTableOperation = CreateStreamingExcelTableOperation<T>(tableExtensionsOperations),
    finishDocumentOperations = FinishStreamingExcelDocumentOperation(),
    headerRowOperation = StreamingExcelRowTableOperation(rowExtensionsOperations),
    rowOperation = StreamingExcelRowTableOperation(rowExtensionsOperations),
    columnOperation = ColumnOperationsWithExtensions(columnExtensionsOperations),
    headerCellOperation = StreamingExcelHeaderCellOperations(cellExtensionsOperations),
    rowCellOperation = StreamingExcelRowCellOperations(cellExtensionsOperations)
)