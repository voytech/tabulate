package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import pl.voytech.exporter.core.model.hints.TableHint
import pl.voytech.exporter.core.model.hints.style.*
import pl.voytech.exporter.core.model.hints.style.enums.BorderStyle
import pl.voytech.exporter.core.model.hints.style.enums.WeightStyle
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.PoiWrapper.cellStyle
import pl.voytech.exporter.impl.template.excel.PoiWrapper.color
import pl.voytech.exporter.impl.template.excel.PoiWrapper.getWorkbook
import pl.voytech.exporter.impl.template.excel.PoiWrapper.tableSheet
import kotlin.reflect.KClass

class CellFontHintOperation: CellHintOperation<CellFontHint> {

    override fun hintType(): KClass<CellFontHint>  = CellFontHint::class

    override fun apply(state: DelegateState, coordinates: Coordinates, hint: CellFontHint) {
        cellStyle(state,coordinates).let {
            val font: XSSFFont = getWorkbook(state).createFont() as XSSFFont
            hint.fontFamily?.run { font.fontName = this }
            hint.fontColor?.run { font.setColor(color(this)) }
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
        cellStyle(state,coordinates).let {
            (it as XSSFCellStyle).setFillForegroundColor(color(hint.color))
            it.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }
}

class CellBordersHintOperation: CellHintOperation<CellBordersHint> {
    override fun hintType(): KClass<CellBordersHint> = CellBordersHint::class

    override fun apply(state: DelegateState, coordinates: Coordinates, hint: CellBordersHint) {
        val toPoiStyle = { style: BorderStyle ->
            when (style) {
                BorderStyle.DASHED -> org.apache.poi.ss.usermodel.BorderStyle.DASHED
                BorderStyle.DOTTED -> org.apache.poi.ss.usermodel.BorderStyle.DOTTED
                BorderStyle.SOLID -> org.apache.poi.ss.usermodel.BorderStyle.THIN
                else -> org.apache.poi.ss.usermodel.BorderStyle.NONE
            }
        }
        cellStyle(state, coordinates).let {
            hint.leftBorderColor?.run { (it as XSSFCellStyle).setLeftBorderColor(color(this)) }
            hint.rightBorderColor?.run { (it as XSSFCellStyle).setRightBorderColor(color(this)) }
            hint.topBorderColor?.run { (it as XSSFCellStyle).setTopBorderColor(color(this)) }
            hint.bottomBorderColor?.run { (it as XSSFCellStyle).setBottomBorderColor(color(this)) }
            hint.leftBorderStyle?.run { it.borderLeft = toPoiStyle(this) }
            hint.rightBorderStyle?.run { it.borderRight = toPoiStyle(this) }
            hint.topBorderStyle?.run { it.borderTop = toPoiStyle(this) }
            hint.bottomBorderStyle?.run{ it.borderBottom = toPoiStyle(this) }
        }
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
    CellBackgroundHintOperation(),
    CellBordersHintOperation()
)

val columnHintsOperations = listOf(
    ColumnWidthHintOperation()
)