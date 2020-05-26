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

internal class StreamingExcelRowTableOperations(rowHints: List<RowHintOperation<out RowHint>>) : RowOperationsWithHints(rowHints) {

    override fun renderHeaderRow(state: DelegateAPI, coordinates: Coordinates) {
        assertRow(state, coordinates)
    }

    override fun renderRow(state: DelegateAPI, coordinates: Coordinates) {
        assertRow(state,coordinates)
    }

}

internal class StreamingExcelLifecycleOperations<T>(): LifecycleOperations<T> {

    override fun create(): DelegateAPI {
        return DelegateAPI(SXSSFWorkbook())
    }

    override fun init(state: DelegateAPI, table: Table<T>): DelegateAPI{
        return getWorkbook(state).createSheet(table.name).let { state }
    }

    override fun complete(state: DelegateAPI): FileData<ByteArray> {
        val outputStream = ByteArrayOutputStream()
        getWorkbook(state).run {
            write(outputStream)
            close()
        }
        return FileData(content = outputStream.toByteArray())
    }

    override fun complete(state: DelegateAPI, stream: OutputStream) {
        getWorkbook(state).run {
            write(stream)
            close()
        }
    }

}

internal class StreamingExcelHeaderCellOperations(cellHints: List<CellHintOperation<out CellHint>>) : HeaderCellOperationsWithHints(cellHints){
    override fun renderHeaderCell(state: DelegateAPI, coordinates: Coordinates, columnTitle: String?) {
        assertCell(state, coordinates).let { cell ->
            columnTitle?.let { cell.setCellValue(it) }
        }
    }

}

internal class StreamingExcelRowCellOperations(cellHints: List<CellHintOperation<out CellHint>>) : RowCellOperationsWithHints(cellHints){

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
                    CellType.NATIVE_FORMULA -> cell.cellFormula = v.value.toString()
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

    override fun renderRowCell(state: DelegateAPI, coordinates: Coordinates, value: CellValue?) {
        assertCell(state,coordinates).also { setCellValue(it,value) }
    }

}

fun <T> excelExport() = ExportOperations(
    StreamingExcelLifecycleOperations<T>(),
    StreamingExcelRowTableOperations(rowHintsOperations),
    ColumnOperationsWithHints(columnHintsOperations),
    StreamingExcelHeaderCellOperations(cellHintsOperations),
    StreamingExcelRowCellOperations(cellHintsOperations)
)