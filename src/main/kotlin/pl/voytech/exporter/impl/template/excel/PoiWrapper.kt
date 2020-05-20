package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateState

object PoiWrapper {
    fun getWorkbook(state: DelegateState): SXSSFWorkbook = state.state as SXSSFWorkbook

    fun tableSheet(state: DelegateState): SXSSFSheet = getWorkbook(state).getSheetAt(0)

    fun createRow(state: DelegateState, rowIndex: Int): SXSSFRow = tableSheet(state).createRow(rowIndex)

    fun row(state: DelegateState, rowIndex: Int): SXSSFRow? = tableSheet(state).getRow(rowIndex)

    fun assertRow(state: DelegateState, rowIndex: Int): SXSSFRow = row(state,rowIndex) ?: createRow(state,rowIndex)

    fun createCell(state: DelegateState, coordinates: Coordinates): SXSSFCell = assertRow(state,coordinates.rowIndex).createCell(coordinates.columnIndex)

    fun cell(state: DelegateState, coordinates: Coordinates): SXSSFCell? = assertRow(state,coordinates.rowIndex).getCell(coordinates.columnIndex)

    fun assertCell(state: DelegateState, coordinates: Coordinates): SXSSFCell = cell(state,coordinates) ?: createCell(state,coordinates)

    fun cellStyle(state: DelegateState, coordinates: Coordinates): CellStyle {
        return assertCell(state,coordinates).let {
                   it.cellStyle = getWorkbook(state).createCellStyle()
                   it.cellStyle
               }
    }

    fun columnStyle(state: DelegateState, columnIndex: Int): CellStyle {
        return tableSheet(state).getColumnStyle(columnIndex)
    }

    fun columnWidth(state: DelegateState, columnIndex: Int): Int {
        return tableSheet(state).getColumnWidth(columnIndex)
    }

    fun setColumnWidth(state: DelegateState, columnIndex: Int, width: Int) {
        return tableSheet(state).setColumnWidth(columnIndex,width)
    }


}
