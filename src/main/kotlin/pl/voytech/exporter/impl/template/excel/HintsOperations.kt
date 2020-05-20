package pl.voytech.exporter.impl.template.excel

import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.model.hints.TableHint
import pl.voytech.exporter.core.model.hints.style.CellBackgroundHint
import pl.voytech.exporter.core.model.hints.style.CellFontHint
import pl.voytech.exporter.core.model.hints.style.ColumnWidthHint
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.cellStyle
import pl.voytech.exporter.impl.template.excel.PoiWrapper.getWorkbook
import pl.voytech.exporter.impl.template.excel.PoiWrapper.tableSheet
import kotlin.reflect.KClass

class CellFontHintOperation: CellHintOperation<CellFontHint> {

    override fun hintType(): KClass<CellFontHint>  = CellFontHint::class

    override fun apply(state: DelegateState, coordinates: Coordinates, hint: CellFontHint) {
        cellStyle(state,coordinates).let {
            val font: XSSFFont = getWorkbook(state).createFont() as XSSFFont
            hint.fontFamily?.run { font.fontName = hint.fontFamily }
            hint.fontColor?.run { font.setColor(XSSFColor(byteArrayOf(hint.fontColor.r.toByte(), hint.fontColor.g.toByte(), hint.fontColor.b.toByte()),null)) }
            hint.fontSize?.run { font.fontHeightInPoints = hint.fontSize.toShort() }
            it.setFont(font)
        }
    }
}

class CellBackgroundHintOperation: CellHintOperation<CellBackgroundHint> {
    override fun hintType(): KClass<CellBackgroundHint> = CellBackgroundHint::class

    override fun apply(state: DelegateState, coordinates: Coordinates, hint: CellBackgroundHint) {
         TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class ColumnWidthHintOperation: ColumnHintOperation<ColumnWidthHint> {
    override fun hintType(): KClass<out ColumnWidthHint> = ColumnWidthHint::class
    override fun apply(state: DelegateState, columnIndex: Int, hint: ColumnWidthHint) = tableSheet(state).setColumnWidth(columnIndex, hint.width)
}

val tableHintsOperations = emptyList<TableHintOperation<TableHint>>()
val rowHintsOperations = emptyList<RowHintOperation<RowHint>>()
val cellHintsOperations = listOf(CellFontHintOperation(), CellBackgroundHintOperation())
val columnHintsOperations = listOf(ColumnWidthHintOperation())