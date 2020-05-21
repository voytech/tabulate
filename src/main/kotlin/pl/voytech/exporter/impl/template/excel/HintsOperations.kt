package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import pl.voytech.exporter.core.model.hints.TableHint
import pl.voytech.exporter.core.model.hints.style.CellBackgroundHint
import pl.voytech.exporter.core.model.hints.style.CellFontHint
import pl.voytech.exporter.core.model.hints.style.ColumnWidthHint
import pl.voytech.exporter.core.model.hints.style.RowHeightHint
import pl.voytech.exporter.core.model.hints.style.enums.WeightStyle
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.PoiWrapper.cellStyle
import pl.voytech.exporter.impl.template.excel.PoiWrapper.getWorkbook
import pl.voytech.exporter.impl.template.excel.PoiWrapper.tableSheet
import kotlin.reflect.KClass

class CellFontHintOperation: CellHintOperation<CellFontHint> {

    override fun hintType(): KClass<CellFontHint>  = CellFontHint::class

    override fun apply(state: DelegateState, coordinates: Coordinates, hint: CellFontHint) {
        cellStyle(state,coordinates).let {
            val font: XSSFFont = getWorkbook(state).createFont() as XSSFFont
            hint.fontFamily?.run { font.fontName = this }
            hint.fontColor?.run { font.setColor(XSSFColor(byteArrayOf(r.toByte(), g.toByte(), b.toByte()),null)) }
            hint.fontSize?.run { font.fontHeightInPoints = toShort() }
            hint.italic?.run { font.italic = this}
            hint.strikeout?.run { font.strikeout = this }
            hint.underline?.let { underline ->  if (underline) font.setUnderline(FontUnderline.SINGLE)  else font.setUnderline(FontUnderline.NONE)}
            hint.weight?.run { font.bold = this == WeightStyle.BOLD }
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
    override fun apply(state: DelegateState, columnIndex: Int, hint: ColumnWidthHint) = tableSheet(state).setColumnWidth(columnIndex, PoiUtils.widthFromPixels(hint.width))
}

class RowHeightHintOperation: RowHintOperation<RowHeightHint> {
    override fun hintType(): KClass<out RowHeightHint> = RowHeightHint::class
    override fun apply(state: DelegateState, rowIndex: Int, hint: RowHeightHint) {
        assertRow(state, rowIndex).height = PoiUtils.heightFromPixels(hint.height)
    }
}


val tableHintsOperations = emptyList<TableHintOperation<TableHint>>()

val rowHintsOperations = listOf(
    RowHeightHintOperation()
)

val cellHintsOperations = listOf(
    CellFontHintOperation(),
    CellBackgroundHintOperation()
)

val columnHintsOperations = listOf(
    ColumnWidthHintOperation()
)