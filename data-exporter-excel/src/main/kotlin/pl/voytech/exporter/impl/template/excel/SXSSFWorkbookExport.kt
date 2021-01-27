package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.ExportOperations
import pl.voytech.exporter.core.template.operations.LifecycleOperations
import pl.voytech.exporter.core.template.operations.chain.TableOperationChain
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareTableOperations
import pl.voytech.exporter.core.template.operations.impl.AttributeCacheTableOperations
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
    override fun createDocument(): SXSSFWorkbook {
        return templateFile?.let { SXSSFWorkbook(WorkbookFactory.create(it) as XSSFWorkbook?, 100) }
            ?: SXSSFWorkbook()
    }

    override fun saveDocument(state: SXSSFWorkbook, stream: OutputStream) {
        workbook(state).run {
            write(stream)
            close()
        }
    }
}

internal class XlsxTableOperations<T> :
    AttributeAwareTableOperations<T, SXSSFWorkbook>(
        tableAttributesOperations,
        columnAttributesOperations<T>(),
        rowAttributesOperations<T>(),
        cellAttributesOperations<T>()
    ) {

    override fun initializeTable(state: SXSSFWorkbook, table: Table<T>): Table<T> {
        return assertTableSheet(state, table.name).let { table }
    }

    override fun renderRowValue(state: SXSSFWorkbook, context: AttributedRow<T>) {
        assertRow(state, context.getTableId(), context.rowIndex)
    }

    override fun renderRowCellValue(
        state: SXSSFWorkbook,
        context: AttributedCell
    ) {
        assertCell(state, context).also {
            setCellValue(it, context.value)
        }.also {
            mergeCells(state, context)
        }
    }

    private fun mergeCells(
        state: SXSSFWorkbook,
        context: AttributedCell
    ) {
        context.takeIf { it.value.colSpan > 1 || it.value.rowSpan > 1 }?.also { cell ->
            (context.rowIndex until context.rowIndex + cell.value.rowSpan).forEach { rowIndex ->
                (context.columnIndex until context.columnIndex + cell.value.colSpan).forEach { colIndex ->
                    assertCell(state, context, rowIndex, colIndex)
                }
            }
            assertTableSheet(state, context.getTableId()).addMergedRegion(
                CellRangeAddress(
                    context.rowIndex,
                    context.rowIndex + cell.value.rowSpan - 1,
                    context.columnIndex,
                    context.columnIndex + cell.value.colSpan - 1
                )
            )
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
