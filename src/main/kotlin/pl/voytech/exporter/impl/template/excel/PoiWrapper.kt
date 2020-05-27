package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFColor
import pl.voytech.exporter.core.model.extension.style.Color
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI

object PoiWrapper {

    fun workbook(state: DelegateAPI): SXSSFWorkbook = state.handle as SXSSFWorkbook

    fun tableSheet(state: DelegateAPI, tableName: String): SXSSFSheet = workbook(state).getSheet(tableName)

    fun createRow(state: DelegateAPI, coordinates: Coordinates): SXSSFRow = tableSheet(state, coordinates.tableName).createRow(coordinates.rowIndex)

    fun row(state: DelegateAPI, coordinates: Coordinates): SXSSFRow? = tableSheet(state, coordinates.tableName).getRow(coordinates.rowIndex)

    fun assertRow(state: DelegateAPI, coordinates: Coordinates): SXSSFRow = row(state, coordinates) ?: createRow(state, coordinates)

    fun createCell(state: DelegateAPI, coordinates: Coordinates): SXSSFCell = assertRow(state, coordinates).let {
        it.createCell(coordinates.columnIndex).also { cell ->
            cell.cellStyle = workbook(state).createCellStyle()
        }
    }

    fun cell(state: DelegateAPI, coordinates: Coordinates): SXSSFCell? = assertRow(state, coordinates).getCell(coordinates.columnIndex)

    fun assertCell(state: DelegateAPI, coordinates: Coordinates): SXSSFCell = cell(state, coordinates) ?: createCell(state,coordinates)

    fun cellStyle(state: DelegateAPI, coordinates: Coordinates): CellStyle {
        return assertCell(state,coordinates).cellStyle
    }

    fun color(color: Color): XSSFColor = XSSFColor(byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte()),null)

    fun columnStyle(state: DelegateAPI, coordinates: Coordinates): CellStyle {
        return tableSheet(state, coordinates.tableName).getColumnStyle(coordinates.columnIndex)
    }

    fun columnWidth(state: DelegateAPI, coordinates: Coordinates): Int {
        return tableSheet(state, coordinates.tableName).getColumnWidth(coordinates.columnIndex)
    }

    fun setColumnWidth(state: DelegateAPI, coordinates: Coordinates, width: Int) {
        return tableSheet(state,coordinates.tableName).setColumnWidth(coordinates.columnIndex,width)
    }

}
