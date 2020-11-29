package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.core.template.operations.AttributeCacheTableOperations
import pl.voytech.exporter.core.template.operations.AttributesHandlingTableOperations
import pl.voytech.exporter.core.template.operations.chain.TableOperationChain
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.assertCell
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.assertTableSheet
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.workbook
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

internal class XlsxLifecycleOperation(private val templateFile: InputStream?) :
    LifecycleOperations<SXSSFWorkbook> {
    override fun createDocument(): DelegateAPI<SXSSFWorkbook> {
        return DelegateAPI(templateFile?.let { SXSSFWorkbook(WorkbookFactory.create(it) as XSSFWorkbook?, 100) }
            ?: SXSSFWorkbook())
    }

    override fun saveDocument(state: DelegateAPI<SXSSFWorkbook>): FileData<ByteArray> {
        val outputStream = ByteArrayOutputStream()
        workbook(state).run {
            write(outputStream)
            close()
        }
        return FileData(content = outputStream.toByteArray())
    }

    override fun saveDocument(state: DelegateAPI<SXSSFWorkbook>, stream: OutputStream) {
        workbook(state).run {
            write(stream)
            close()
        }
    }
}

internal class XlsxTableOperations<T> :
    AttributesHandlingTableOperations<T, SXSSFWorkbook>(
        tableAttributesOperations,
        columnAttributesOperations<T>(),
        rowAttributesOperations<T>(),
        cellAttributesOperations<T>()
    ) {

    override fun initializeTable(state: DelegateAPI<SXSSFWorkbook>, table: Table<T>): Table<T> {
        return assertTableSheet(state, table.name).let { table }
    }

    override fun renderRowValue(state: DelegateAPI<SXSSFWorkbook>, context: OperationContext<T, RowOperationTableData<T>>) {
        assertRow(state, context.coordinates!!)
    }

    override fun renderRowCellValue(
        state: DelegateAPI<SXSSFWorkbook>,
        context: OperationContext<T, CellOperationTableData<T>>
    ) {
        assertCell(state, context.coordinates!!, context).also {
            setCellValue(it, context.data.cellValue?.value)
        }.also {
            mergeCells(state, context)
        }
    }

    private fun mergeCells(
        state: DelegateAPI<SXSSFWorkbook>,
        context: OperationContext<T, CellOperationTableData<T>>
    ) {
        context.data.cellValue?.takeIf { it.value.colSpan > 1 || it.value.rowSpan > 1 }?.also { cell ->
            context.coordinates!!.also { coordinates ->
                (coordinates.rowIndex until coordinates.rowIndex + cell.value.rowSpan).forEach { rowIndex ->
                    (coordinates.columnIndex until coordinates.columnIndex + cell.value.colSpan).forEach { colIndex ->
                        assertCell(state, Coordinates(coordinates.tableName, rowIndex, colIndex), context)
                    }
                }
                assertTableSheet(state, coordinates.tableName).addMergedRegion(
                    CellRangeAddress(
                        coordinates.rowIndex,
                        coordinates.rowIndex + cell.value.rowSpan - 1,
                        coordinates.columnIndex,
                        coordinates.columnIndex + cell.value.colSpan - 1
                    )
                )
            }
        }
    }

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
}


fun <T> xlsxExport(templateFile: InputStream? = null): ExportOperations<T, SXSSFWorkbook> =
    ExportOperations(
        lifecycleOperations = XlsxLifecycleOperation(templateFile),
        tableOperations = TableOperationChain(
            AttributeCacheTableOperations(), XlsxTableOperations()
        )
    )
