package pl.voytech.exporter.impl.template.excel

import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertCell
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.PoiWrapper.getWorkbook
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

internal class StreamingExcelBasicTableOperations<T>(rowHints: List<RowHintOperation<out RowHint>>) : BasicOperationsWithHints<T>(rowHints) {
    override fun init(table: Table<T>): DelegateState {
        return DelegateState(SXSSFWorkbook().also {
            it.createSheet(table.name)
        })
    }

    override fun renderHeaderRow(state: DelegateState, coordinates: Coordinates) {
        assertRow(state, coordinates.rowIndex)
    }

    override fun renderRow(state: DelegateState, coordinates: Coordinates) {
        assertRow(state,coordinates.rowIndex)
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

internal class StreamingExcelHeaderCellOperation(cellHints: List<CellHintOperation<out CellHint>>) : HeaderCellOperationWithHints(cellHints){
    override fun renderHeaderCell(state: DelegateState, coordinates: Coordinates, columnTitle: String?) {
        assertCell(state, coordinates).let { cell ->
            columnTitle?.let { cell.setCellValue(it) }
        }
    }

}

internal class StreamingExcelRowCellOperation(cellHints: List<CellHintOperation<out CellHint>>) : RowCellOperationWithHints(cellHints){

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

    override fun renderRowCell(state: DelegateState, coordinates: Coordinates, value: CellValue?) {
        assertCell(state,coordinates).also { setCellValue(it,value) }
    }

}

fun <T> excelExport()  = ExportOperations(
    StreamingExcelBasicTableOperations<T>(rowHintsOperations),
    ColumnOperationWithHints(columnHintsOperations),
    StreamingExcelHeaderCellOperation(cellHintsOperations),
    StreamingExcelRowCellOperation(cellHintsOperations)
)