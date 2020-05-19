package pl.voytech.exporter.impl.template.excel

import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.getWorkbook
import pl.voytech.exporter.impl.template.excel.PoiWrapper.tableSheet
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class SXSSFWorkbookExport<T>() : HintsResolvingExportOperations<T>(tableHintsOperations, rowHintsOperations, cellHintsOperations) {

    private fun toDateValue(value: Any): Date {
        return when (value) {
            is LocalDate -> Date.from(value.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
            is LocalDateTime -> Date.from(value.atZone(ZoneId.systemDefault()).toInstant())
            is Date -> value
            is String -> Date.from(Instant.parse(value))
            else -> throw IllegalArgumentException()
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
                    CellType.NATIVE_FORMULA -> TODO()
                    CellType.FORMULA -> TODO()
                    CellType.ERROR -> TODO()
                }
            } ?: v.run {
                when(this.value){
                    is String -> cell.setCellValue(this.value)
                    is Boolean -> cell.setCellValue(this.value)
                    is LocalDate -> cell.setCellValue(toDateValue(this.value))
                    is LocalDateTime -> cell.setCellValue(toDateValue(this.value))
                    is Date -> cell.setCellValue(this.value)
                    is Number -> cell.setCellValue(this.value.toDouble())
                }
            }
        }
    }

    override fun init(table: Table<T>): DelegateState {
        return DelegateState(SXSSFWorkbook().also { it.createSheet(table.name) })
    }

    override fun renderColumnsTitlesRow(state: DelegateState, coordinates: Coordinates) {
        tableSheet(state).createRow(coordinates.rowIndex)
    }

    override fun renderColumnTitleCell(
        state: DelegateState,
        coordinates: Coordinates,
        columnTitle: String?
    ) {
        tableSheet(state).getRow(0).createCell(coordinates.columnIndex).let { cell ->
            columnTitle?.let { cell.setCellValue(it) }
        }
    }

    override fun renderRow(state: DelegateState, coordinates: Coordinates) {
        tableSheet(state).createRow(coordinates.rowIndex)
    }

    override fun renderRowCell(state: DelegateState, coordinates: Coordinates, value: CellValue?) {
        tableSheet(state).getRow(coordinates.rowIndex).createCell(coordinates.columnIndex).also { setCellValue(it,value) }
    }

    override fun complete(state: DelegateState, coordinates: Coordinates): FileData<ByteArray> {
        val outputStream = ByteArrayOutputStream()
        getWorkbook(state).run {
            write(outputStream)
            close()
        }
        return FileData(content = outputStream.toByteArray())
    }

    override fun complete(state: DelegateState, coordinates: Coordinates, stream: OutputStream) {
        getWorkbook(state).run {
            write(stream)
            close()
        }
    }
}