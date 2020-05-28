package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.RowExtension
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertCell
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertTableSheet
import pl.voytech.exporter.impl.template.excel.PoiWrapper.workbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

internal class StreamingExcelRowTableOperation(rowHints: List<RowExtensionOperation<out RowExtension>>) : RowOperationWithExtensions(rowHints) {

    override fun renderRow(state: DelegateAPI, coordinates: Coordinates) {
        assertRow(state, coordinates)
    }

}

internal class CreateStreamingExcelDocumentOperation(private val templateFile: String?) : CreateDocumentOperation {
    override fun createDocument(): DelegateAPI {
        return DelegateAPI(templateFile?.let { SXSSFWorkbook(WorkbookFactory.create(File(it)) as XSSFWorkbook?,100) } ?: SXSSFWorkbook())
    }
}

internal class CreateStreamingExcelTableOperation<T>: CreateTableOperation<T> {
    override fun createTable(state: DelegateAPI, table: Table<T>): DelegateAPI {
        return assertTableSheet(state, table.name).let { state }
    }
}

internal class FinishStreamingExcelDocumentOperation: FinishDocumentOperations {
    override fun finishDocument(state: DelegateAPI): FileData<ByteArray> {
        val outputStream = ByteArrayOutputStream()
        workbook(state).run {
            write(outputStream)
            close()
        }
        return FileData(content = outputStream.toByteArray())
    }

    override fun finishDocument(state: DelegateAPI, stream: OutputStream) {
        workbook(state).run {
            write(stream)
            close()
        }
    }
}

internal class StreamingExcelHeaderCellOperations(cellExtensions: List<CellExtensionOperation<out CellExtension>>) : HeaderCellOperationsWithExtensions(cellExtensions){
    override fun renderHeaderCell(state: DelegateAPI, coordinates: Coordinates, columnTitle: String?) {
        assertCell(state, coordinates).let { cell ->
            columnTitle?.let { cell.setCellValue(it) }
        }
    }

}

internal class StreamingExcelRowCellOperations(cellExtensions: List<CellExtensionOperation<out CellExtension>>) : RowCellOperationsWithExtensions(cellExtensions){

    private fun toDateValue(value: Any): Date {
        return when (value) {
            is LocalDate -> Date.from(value.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
            is LocalDateTime -> Date.from(value.atZone(ZoneId.systemDefault()).toInstant())
            is Date -> value
            is String -> Date.from(Instant.parse(value))
            else -> throw IllegalArgumentException()
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
                    CellType.FORMULA -> TODO()
                    CellType.ERROR -> TODO()
                }
            } ?: v.run {
                when(this.value){
                    is String -> cell.setCellValue(this.value)
                    is Boolean -> cell.setCellValue(this.value)
                    is LocalDate -> cell.setCellValue(toDateValue(this.value))
                    is LocalDateTime -> cell.setCellValue(toDateValue(this.value))
                    is Date -> cell.setCellValue(this.value)
                    is Number -> cell.setCellValue(this.value.toDouble())
                }
            }
        }
    }

    override fun renderRowCell(state: DelegateAPI, coordinates: Coordinates, value: CellValue?) {
        assertCell(state,coordinates).also { setCellValue(it,value) }
    }

}

fun <T> excelExport(templateFile: String? = null) = ExportOperations(
    lifecycleOperations = LifecycleOperations(
        createDocumentOperation = CreateStreamingExcelDocumentOperation(templateFile),
        createTableOperation = CreateStreamingExcelTableOperation<T>(),
        finishDocumentOperations = FinishStreamingExcelDocumentOperation()
    ),
    headerRowOperation = StreamingExcelRowTableOperation(rowExtensionsOperations),
    rowOperation = StreamingExcelRowTableOperation(rowExtensionsOperations),
    columnOperation = ColumnOperationsWithExtensions(columnExtensionsOperations),
    headerCellOperation = StreamingExcelHeaderCellOperations(cellExtensionsOperations),
    rowCellOperation = StreamingExcelRowCellOperations(cellExtensionsOperations)
)