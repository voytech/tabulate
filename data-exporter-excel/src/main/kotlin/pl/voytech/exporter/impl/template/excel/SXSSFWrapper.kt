package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.CellStyle
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
import pl.voytech.exporter.core.template.operations.impl.AttributeKeyDrivenCache.Companion.getCellCachedValue
import pl.voytech.exporter.core.template.operations.impl.AttributeKeyDrivenCache.Companion.putCellCachedValue

object SXSSFWrapper {

    private const val CELL_STYLE_CACHE_KEY = "cellStyle"

    fun workbook(state: SXSSFWorkbook): SXSSFWorkbook = state

    private fun nonStreamingWorkbook(state: SXSSFWorkbook): XSSFWorkbook = state.xssfWorkbook

    fun tableSheet(state: SXSSFWorkbook, tableName: String): SXSSFSheet =
        workbook(state).getSheet(tableName)

    fun assertTableSheet(state: SXSSFWorkbook, tableName: String?): SXSSFSheet =
        workbook(state).getSheet(tableName) ?: workbook(state).createSheet(tableName)

    fun assertRow(state: SXSSFWorkbook, tableId: String, rowIndex: Int): SXSSFRow =
        row(state, tableId, rowIndex) ?: createRow(state, tableId, rowIndex)

    fun xssfCell(state: SXSSFWorkbook, coordinates: Coordinates): XSSFCell? =
        nonStreamingWorkbook(state)
            .getSheet(coordinates.tableName)
            .getRow(coordinates.rowIndex)
            .getCell(coordinates.columnIndex)

    fun assertCell(
        state: SXSSFWorkbook,
        context: AttributedCell,
        rowIndex: Int,
        columnIndex: Int
    ): SXSSFCell =
        cell(state, context.getTableId(), rowIndex, columnIndex) ?: createCell(state, context, rowIndex, columnIndex)

    fun assertCell(state: SXSSFWorkbook, context: AttributedCell): SXSSFCell =
        cell(state, context.getTableId(), context.rowIndex, context.columnIndex) ?: createCell(state, context)

    fun cellStyle(
        state: SXSSFWorkbook,
        context: AttributedCell
    ): CellStyle {
        return assertCell(state, context).cellStyle
    }

    fun color(color: Color): XSSFColor =
        XSSFColor(byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte()), null)

    private fun cell(state: SXSSFWorkbook, tableId: String, rowIndex: Int, columnIndex: Int): SXSSFCell? =
        assertRow(state, tableId, rowIndex).getCell(columnIndex)

    private fun createCell(
        state: SXSSFWorkbook,
        context: AttributedCell,
        alterRowIndex: Int? = null,
        alterColumnIndex: Int? = null
    ): SXSSFCell =
        assertRow(state, context.getTableId(), alterRowIndex ?: context.rowIndex).let {
            it.createCell(alterColumnIndex ?: context.columnIndex).also { cell ->
                val cellStyle = getCellCachedValue(context, CELL_STYLE_CACHE_KEY) ?: putCellCachedValue(
                    context,
                    CELL_STYLE_CACHE_KEY,
                    workbook(state).createCellStyle()
                )
                cell.cellStyle = cellStyle as CellStyle
            }
        }

    private fun createRow(state: SXSSFWorkbook, tableId: String, rowIndex: Int): SXSSFRow =
        tableSheet(state, tableId).createRow(rowIndex)

    private fun row(state: SXSSFWorkbook, tableId: String, rowIndex: Int): SXSSFRow? =
        tableSheet(state, tableId).getRow(rowIndex)

}
