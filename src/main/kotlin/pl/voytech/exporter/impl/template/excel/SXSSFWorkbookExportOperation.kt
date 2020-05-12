package pl.voytech.exporter.impl.template.excel

import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.template.*
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime

class SXSSFWorkbookExportOperation<T> : ExportOperations<T> {

    private fun castState(state: ExportingState): SXSSFWorkbook = state.delegate.state as SXSSFWorkbook

    private fun tableSheet(state: ExportingState): SXSSFSheet = castState(state).getSheetAt(0)

    private fun toDateValue(value: Any) {
        when (value) {
            is LocalDate -> (value as LocalDate)
            is LocalDateTime -> (value as LocalDateTime)
        }
    }

    private fun setCellValue(cell: SXSSFCell, value: CellValue?) {
        if (value!= null) {
            when (value.type) {
                CellType.STRING -> cell.setCellValue(value.value as String)
                CellType.BOOLEAN -> cell.setCellValue(value.value as Boolean)
                //CellType.DATE -> cell.setCellValue(toDateValue(value.value))
                CellType.NUMERIC -> cell.setCellValue(value as Double)
            }
        }
    }

    override fun init(table: Table<T>): DelegateState {
        val workbook = SXSSFWorkbook()
        workbook.createSheet(table.name)
        return DelegateState(workbook)
    }

    override fun renderColumnsTitlesRow(state: ExportingState): ExportingState {
        val sheet = tableSheet(state)
        sheet.createRow(0)
        return state
    }

    override fun renderColumnTitleCell(
        state: ExportingState,
        columnTitle: Description?,
        cellHints: List<CellHint>?
    ): ExportingState {
        val sheet = tableSheet(state)
        val cell = sheet.getRow(0).createCell(state.columnIndex)
        if (columnTitle != null) {
            cell.setCellValue(columnTitle.title)
        }
        return state
    }

    override fun renderRow(state: ExportingState, rowHints: List<RowHint>?): ExportingState {
        val sheet = tableSheet(state)
        sheet.createRow(state.rowIndex+1)
        return state
    }

    override fun renderRowCell(state: ExportingState, value: CellValue?, cellHints: List<CellHint>?): ExportingState {
        val sheet = tableSheet(state)
        val cell = sheet.getRow(state.rowIndex+1).createCell(state.columnIndex)
        setCellValue(cell, value)
        return state
    }

    override fun complete(state: ExportingState): FileData<ByteArray> {
        val outputStream = ByteArrayOutputStream()
        val workbook = (state.delegate.state as XSSFWorkbook)
        workbook.write(outputStream)
        workbook.close()
        return FileData(content = outputStream.toByteArray())
    }

    override fun complete(state: ExportingState, stream: OutputStream) {
        val workbook = (state.delegate.state as XSSFWorkbook)
        workbook.write(stream)
        workbook.close()
    }
}