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
import pl.voytech.exporter.core.template.CellOperationTableData
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI
import pl.voytech.exporter.core.template.OperationContext
import pl.voytech.exporter.core.template.operations.chain.ExtensionKeyDrivenCache.Companion.getCellCachedValue
import pl.voytech.exporter.core.template.operations.chain.ExtensionKeyDrivenCache.Companion.putCellCachedValue

object SXSSFWrapper {

    const val CELL_STYLE_CACHE_KEY = "cellStyle"

    fun workbook(state: DelegateAPI<SXSSFWorkbook>): SXSSFWorkbook = state.handle

    fun nonStreamingWorkbook(state: DelegateAPI<SXSSFWorkbook>): XSSFWorkbook = state.handle.xssfWorkbook

    fun tableSheet(state: DelegateAPI<SXSSFWorkbook>, tableName: String): SXSSFSheet =
        workbook(state).getSheet(tableName)

    fun assertTableSheet(state: DelegateAPI<SXSSFWorkbook>, tableName: String?): SXSSFSheet =
        workbook(state).getSheet(tableName) ?: workbook(state).createSheet(tableName)

    fun assertRow(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFRow =
        row(state, coordinates) ?: createRow(state, coordinates)

    fun xssfCell(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): XSSFCell? =
        nonStreamingWorkbook(state)
            .getSheet(coordinates.tableName)
            .getRow(coordinates.rowIndex)
            .getCell(coordinates.columnIndex)

    fun <T> assertCell(
        state: DelegateAPI<SXSSFWorkbook>,
        coordinates: Coordinates,
        context: OperationContext<T, CellOperationTableData<T>>
    ): SXSSFCell =
        cell(state, coordinates) ?: createCell(state, coordinates, context)

    fun <T> cellStyle(
        state: DelegateAPI<SXSSFWorkbook>,
        coordinates: Coordinates,
        context: OperationContext<T, CellOperationTableData<T>>
    ): CellStyle {
        return assertCell(state, coordinates, context).cellStyle
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

    private fun cell(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFCell? =
        assertRow(state, coordinates).getCell(coordinates.columnIndex)

    private fun <T> createCell(
        state: DelegateAPI<SXSSFWorkbook>,
        coordinates: Coordinates,
        context: OperationContext<T, CellOperationTableData<T>>
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

    private fun createRow(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFRow =
        tableSheet(state, coordinates.tableName).createRow(coordinates.rowIndex)

    private fun row(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): SXSSFRow? =
        tableSheet(state, coordinates.tableName).getRow(coordinates.rowIndex)

}
