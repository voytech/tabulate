package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.extension.style.Color
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI

object PoiWrapper {

    fun workbook(state: DelegateAPI<SXSSFWorkbook>): SXSSFWorkbook = state.handle

    fun xssfWorkbook(state: DelegateAPI<SXSSFWorkbook>): XSSFWorkbook = state.handle.xssfWorkbook

    fun tableSheet(state: DelegateAPI<SXSSFWorkbook>, tableName: String): SXSSFSheet =
        workbook(state).getSheet(tableName)

    fun assertTableSheet(state: DelegateAPI<SXSSFWorkbook>, tableName: String?): SXSSFSheet =
        workbook(state).getSheet(tableName) ?: workbook(state).createSheet(tableName)

    fun createRow(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFRow =
        tableSheet(state, coordinates.tableName).createRow(coordinates.rowIndex)

    fun row(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFRow? =
        tableSheet(state, coordinates.tableName).getRow(coordinates.rowIndex)

    fun assertRow(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFRow =
        row(state, coordinates) ?: createRow(state, coordinates)

    fun createCell(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFCell =
        assertRow(state, coordinates).let {
            it.createCell(coordinates.columnIndex).also { cell ->
                cell.cellStyle = workbook(state).createCellStyle()
            }
        }

    fun xssfCell(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): XSSFCell? =
        xssfWorkbook(state)
            .getSheet(coordinates.tableName)
            .getRow(coordinates.rowIndex)
            .getCell(coordinates.columnIndex)


    fun cell(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFCell? =
        assertRow(state, coordinates).getCell(coordinates.columnIndex)

    fun assertCell(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFCell =
        cell(state, coordinates) ?: createCell(state, coordinates)

    fun cellStyle(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellStyle {
        return assertCell(state, coordinates).cellStyle
    }

    fun color(color: Color): XSSFColor =
        XSSFColor(byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte()), null)

    fun columnStyle(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellStyle {
        return tableSheet(state, coordinates.tableName).getColumnStyle(coordinates.columnIndex)
    }

    fun columnWidth(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): Int {
        return tableSheet(state, coordinates.tableName).getColumnWidth(coordinates.columnIndex)
    }

    fun setColumnWidth(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, width: Int) {
        return tableSheet(state, coordinates.tableName).setColumnWidth(coordinates.columnIndex, width)
    }

}
