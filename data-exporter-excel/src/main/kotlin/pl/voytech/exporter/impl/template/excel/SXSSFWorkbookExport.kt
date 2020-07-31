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
import pl.voytech.exporter.core.template.operations.chain.*
import pl.voytech.exporter.core.template.operations.chain.ExtensionsCacheOperationsFactory.extensionCacheColumnOperation
import pl.voytech.exporter.core.template.operations.chain.ExtensionsCacheOperationsFactory.extensionCacheRowCellOperation
import pl.voytech.exporter.core.template.operations.chain.ExtensionsCacheOperationsFactory.extensionCacheRowOperation
import pl.voytech.exporter.core.template.operations.chain.RowOperations
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

internal class XlsxRowTableOperation<T>(rowHints: List<RowExtensionOperation<T, out RowExtension, SXSSFWorkbook>>) :
    ExtensionDispatchingRowOperation<T, SXSSFWorkbook>(rowHints) {

    override fun renderRow(state: DelegateAPI<SXSSFWorkbook>, context: OperationContext<T, RowOperationTableData<T>>) {
        assertRow(state, context.coordinates!!)
    }

}

internal class XlsxCreateDocumentOperation(private val templateFile: InputStream?) :
    CreateDocumentOperation<SXSSFWorkbook> {
    override fun createDocument(): DelegateAPI<SXSSFWorkbook> {
        return DelegateAPI(templateFile?.let { SXSSFWorkbook(WorkbookFactory.create(it) as XSSFWorkbook?, 100) }
            ?: SXSSFWorkbook())
    }
}

internal class XlsxCreateTableOperation<T>(tableExtensionOperations: List<TableExtensionOperation<out TableExtension, SXSSFWorkbook>>) :
    ExtensionDispatchingCreateTableOperation<T, SXSSFWorkbook>(tableExtensionOperations) {
    override fun initializeTable(state: DelegateAPI<SXSSFWorkbook>, table: Table<T>): DelegateAPI<SXSSFWorkbook> {
        return assertTableSheet(state, table.name).let { state }
    }
}

internal class XlsxFinishDocumentOperation : FinishDocumentOperations<SXSSFWorkbook> {
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

internal class XlsxRowCellOperations<T>(cellExtensions: List<CellExtensionOperation<T, out CellExtension, SXSSFWorkbook>>) :
    ExtensionDispatchingRowCellOperation<T, SXSSFWorkbook>(cellExtensions) {

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

    override fun renderRowCell(
        state: DelegateAPI<SXSSFWorkbook>,
        context: OperationContext<T, CellOperationTableData<T>>
    ) {
        assertCell(state, context.coordinates!!).also { setCellValue(it, context.value.cellValue) }
    }

}

fun <T> xlsxExport(templateFile: InputStream? = null) = ExportOperations(
    createDocumentOperation = XlsxCreateDocumentOperation(templateFile),
    createTableOperation = XlsxCreateTableOperation(tableExtensionsOperations),
    finishDocumentOperations = XlsxFinishDocumentOperation(),
    rowOperation = RowOperations(
        extensionCacheRowOperation(),
        XlsxRowTableOperation<T>(rowExtensionsOperations())
    ),
    columnOperation = ColumnOperations(
        extensionCacheColumnOperation(),
        ExtensionDispatchingColumnOperation(columnExtensionsOperations())
    ),
    rowCellOperation = RowCellOperations(
        extensionCacheRowCellOperation(),
        XlsxRowCellOperations(cellExtensionsOperations())
    )
)