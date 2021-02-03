package pl.voytech.exporter.impl.template.excel.wrapper

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.attributes.style.Color
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.Coordinates
import pl.voytech.exporter.core.template.operations.impl.putCachedValueIfAbsent
import java.io.InputStream

class ApachePoiExcelFacade(templateFile: InputStream? = null) {

    private val CELL_STYLE_CACHE_KEY: String = "cellStyle"

    private val adaptee: SXSSFWorkbook = if (templateFile != null) {
        SXSSFWorkbook(WorkbookFactory.create(templateFile) as XSSFWorkbook?, 100)
    } else {
        SXSSFWorkbook()
    }

    fun workbook(): SXSSFWorkbook = adaptee

    fun tableSheet(tableName: String): SXSSFSheet =
        workbook().getSheet(tableName)

    fun assertTableSheet(tableName: String?): SXSSFSheet =
        workbook().getSheet(tableName) ?: workbook().createSheet(tableName)

    fun assertRow(tableId: String, rowIndex: Int): SXSSFRow =
        row(tableId, rowIndex) ?: createRow(tableId, rowIndex)

    fun xssfCell(coordinates: Coordinates): XSSFCell? =
        adaptee.xssfWorkbook
            .getSheet(coordinates.tableName)
            .getRow(coordinates.rowIndex)
            .getCell(coordinates.columnIndex)

    fun assertCell(
        context: AttributedCell,
        rowIndex: Int,
        columnIndex: Int
    ): SXSSFCell =
        cell(context.getTableId(), rowIndex, columnIndex) ?: createCell(context, rowIndex, columnIndex)

    fun assertCell(context: AttributedCell): SXSSFCell =
        cell(context.getTableId(), context.rowIndex, context.columnIndex) ?: createCell(context)

    fun cellStyle(context: AttributedCell): CellStyle = assertCell(context).cellStyle

    private fun cell(tableId: String, rowIndex: Int, columnIndex: Int): SXSSFCell? =
        assertRow(tableId, rowIndex).getCell(columnIndex)

    private fun createCell(
        context: AttributedCell,
        alterRowIndex: Int? = null,
        alterColumnIndex: Int? = null
    ): SXSSFCell =
        assertRow(context.getTableId(), alterRowIndex ?: context.rowIndex).let {
            it.createCell(alterColumnIndex ?: context.columnIndex).also { cell ->
                cell.cellStyle =
                    context.putCachedValueIfAbsent(CELL_STYLE_CACHE_KEY, workbook().createCellStyle()) as CellStyle
            }
        }

    private fun createRow(tableId: String, rowIndex: Int): SXSSFRow =
        tableSheet(tableId).createRow(rowIndex)

    private fun row(tableId: String, rowIndex: Int): SXSSFRow? =
        tableSheet(tableId).getRow(rowIndex)

    companion object {
        fun color(color: Color): XSSFColor =
            XSSFColor(byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte()), null)
    }

}
