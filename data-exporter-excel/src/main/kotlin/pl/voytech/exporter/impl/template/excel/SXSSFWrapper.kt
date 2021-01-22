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
import pl.voytech.exporter.core.template.context.OperationContext
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

    fun assertRow(state: SXSSFWorkbook, coordinates: Coordinates): SXSSFRow =
        row(state, coordinates) ?: createRow(state, coordinates)

    fun xssfCell(state: SXSSFWorkbook, coordinates: Coordinates): XSSFCell? =
        nonStreamingWorkbook(state)
            .getSheet(coordinates.tableName)
            .getRow(coordinates.rowIndex)
            .getCell(coordinates.columnIndex)

    fun assertCell(
        state: SXSSFWorkbook,
        coordinates: Coordinates,
        context: OperationContext<AttributedCell>
    ): SXSSFCell =
        cell(state, coordinates) ?: createCell(state, coordinates, context)

    fun assertCell(
        state: SXSSFWorkbook,
        coordinates: Coordinates
    ): SXSSFCell =
        cell(state, coordinates) ?: createCell(state, coordinates)

    fun cellStyle(
        state: SXSSFWorkbook,
        coordinates: Coordinates,
        context: OperationContext<AttributedCell>
    ): CellStyle {
        return assertCell(state, coordinates, context).cellStyle
    }

    fun color(color: Color): XSSFColor =
        XSSFColor(byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte()), null)

    fun columnStyle(state: SXSSFWorkbook, coordinates: Coordinates): CellStyle {
        return tableSheet(state, coordinates.tableName).getColumnStyle(coordinates.columnIndex)
    }

    fun columnWidth(state: SXSSFWorkbook, coordinates: Coordinates): Int {
        return tableSheet(state, coordinates.tableName).getColumnWidth(coordinates.columnIndex)
    }

    fun setColumnWidth(state: SXSSFWorkbook, coordinates: Coordinates, width: Int) {
        return tableSheet(state, coordinates.tableName).setColumnWidth(coordinates.columnIndex, width)
    }

    private fun cell(state: SXSSFWorkbook, coordinates: Coordinates): SXSSFCell? =
        assertRow(state, coordinates).getCell(coordinates.columnIndex)

    private fun createCell(
        state: SXSSFWorkbook,
        coordinates: Coordinates,
        context: OperationContext<AttributedCell>
    ): SXSSFCell =
        assertRow(state, coordinates).let {
            it.createCell(coordinates.columnIndex).also { cell ->
                val cellStyle = getCellCachedValue(context, CELL_STYLE_CACHE_KEY) ?: putCellCachedValue(
                    context,
                    CELL_STYLE_CACHE_KEY,
                    workbook(state).createCellStyle()
                )
                cell.cellStyle = cellStyle as CellStyle
            }
        }

    private fun createCell(
        state: SXSSFWorkbook,
        coordinates: Coordinates
    ): SXSSFCell =
        assertRow(state, coordinates).let {
            it.createCell(coordinates.columnIndex)
        }

    private fun createRow(state: SXSSFWorkbook, coordinates: Coordinates): SXSSFRow =
        tableSheet(state, coordinates.tableName).createRow(coordinates.rowIndex)

    private fun row(state: SXSSFWorkbook, coordinates: Coordinates): SXSSFRow? =
        tableSheet(state, coordinates.tableName).getRow(coordinates.rowIndex)

}
