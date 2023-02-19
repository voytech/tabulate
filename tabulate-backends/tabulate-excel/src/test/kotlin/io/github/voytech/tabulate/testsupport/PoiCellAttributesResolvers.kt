package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.components.table.model.attributes.cell.TypeHintAttribute
import io.github.voytech.tabulate.components.table.operation.Coordinates
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.model.background.DefaultFillType
import io.github.voytech.tabulate.core.model.background.FillType
import io.github.voytech.tabulate.core.model.border.BorderStyle
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.text.DefaultFonts
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.excel.components.table.model.ExcelBorderStyle
import io.github.voytech.tabulate.excel.components.table.model.ExcelCellFills
import io.github.voytech.tabulate.excel.components.table.model.ExcelTypeHints
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellCommentAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.PrintingAttribute
import io.github.voytech.tabulate.test.*
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.util.Units
import org.apache.poi.xssf.usermodel.XSSFColor
import kotlin.math.roundToInt
import org.apache.poi.ss.usermodel.BorderStyle as PoiBorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment as PoiHorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment as PoiVerticalAlignment

private fun parseColor(xssfColor: XSSFColor) =
    Color(
        xssfColor.rgb[0].toInt().and(0xFF),
        xssfColor.rgb[1].toInt().and(0xFF),
        xssfColor.rgb[2].toInt().and(0xFF)
    )

class PoiCellFontAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, CellPosition> {

    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): Attribute<*> {
        return api.xssfCell(Coordinates(tableId, select.rowIndex, select.columnIndex)).let { cell ->
            TextStylesAttribute(
                fontFamily = cell?.cellStyle?.font?.fontName
                    ?.replace(" ","_")
                    ?.uppercase()
                    ?.let { DefaultFonts.valueOf(it) },
                fontSize = cell?.cellStyle?.font?.fontHeightInPoints?.toInt(),
                fontColor = cell?.cellStyle?.font?.xssfColor?.let { parseColor(it) },
                weight = cell?.cellStyle?.font?.bold?.let {
                    if (it) DefaultWeightStyle.BOLD else DefaultWeightStyle.NORMAL
                },
                strikeout = cell?.cellStyle?.font?.strikeout,
                underline = cell?.cellStyle?.font?.underline == Font.U_SINGLE,
                italic = cell?.cellStyle?.font?.italic,
                rotation = cell?.cellStyle?.rotation,
                ident = cell?.cellStyle?.indention,
                wrapText = cell?.cellStyle?.wrapText
            )
        }
    }
}

class PoiCellBackgroundAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): Attribute<*> {
        return with(api.xssfCell(Coordinates(tableId,select.rowIndex,select.columnIndex))?.cellStyle) {
            if (this != null) {
                BackgroundAttribute(
                    color = fillForegroundXSSFColor?.let { parseColor(it) },
                    fill = fillPattern.let {
                        when (it) {
                            FillPatternType.BIG_SPOTS -> DefaultFillType.LARGE_SPOTS
                            FillPatternType.SQUARES -> DefaultFillType.SQUARES
                            FillPatternType.FINE_DOTS -> DefaultFillType.SMALL_DOTS
                            FillPatternType.ALT_BARS -> DefaultFillType.WIDE_DOTS
                            FillPatternType.DIAMONDS -> DefaultFillType.DIAMONDS
                            FillPatternType.BRICKS -> DefaultFillType.BRICKS
                            FillPatternType.SOLID_FOREGROUND -> DefaultFillType.SOLID
                            else -> resolveCellFillPattern(it)
                        }
                    } ?: DefaultFillType.SOLID
                )
            } else BackgroundAttribute()
        }
    }

    private fun resolveCellFillPattern(fillPattern: FillPatternType?): FillType? {
        return try {
            if (fillPattern != null) ExcelCellFills.valueOf(fillPattern.name) else DefaultFillType.SOLID
        } catch (e: IllegalArgumentException) {
            DefaultFillType.SOLID
        }
    }
}

class PoiCellBordersAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): Attribute<*> {

        return api.xssfCell(Coordinates(tableId,select.rowIndex,select.columnIndex)).let { cell ->
            BordersAttribute(
                leftBorderStyle = cell?.cellStyle?.borderLeft?.let { resolveBorderStyle(it) },
                leftBorderColor = cell?.cellStyle?.leftBorderXSSFColor?.let { parseColor(it) },
                rightBorderStyle = cell?.cellStyle?.borderRight?.let { resolveBorderStyle(it) },
                rightBorderColor = cell?.cellStyle?.rightBorderXSSFColor?.let { parseColor(it) },
                topBorderStyle = cell?.cellStyle?.borderTop?.let { resolveBorderStyle(it) },
                topBorderColor = cell?.cellStyle?.topBorderXSSFColor?.let { parseColor(it) },
                bottomBorderStyle = cell?.cellStyle?.borderBottom?.let { resolveBorderStyle(it) },
                bottomBorderColor = cell?.cellStyle?.bottomBorderXSSFColor?.let { parseColor(it) }
            )
        }
    }

    private fun resolveBorderStyle(style: PoiBorderStyle): BorderStyle {
        return when (style) {
            PoiBorderStyle.DASHED -> DefaultBorderStyle.DASHED
            PoiBorderStyle.DOTTED -> DefaultBorderStyle.DOTTED
            PoiBorderStyle.THIN -> DefaultBorderStyle.SOLID
            PoiBorderStyle.DOUBLE -> DefaultBorderStyle.DOUBLE
            PoiBorderStyle.NONE -> DefaultBorderStyle.NONE
            else -> try {
                ExcelBorderStyle.valueOf(style.name)
            } catch (e: IllegalArgumentException) {
                DefaultBorderStyle.NONE
            }
        }
    }


}

class PoiCellDataFormatAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): Attribute<*> {
        return api.xssfCell(Coordinates(tableId,select.rowIndex,select.columnIndex)).let {
            it?.cellStyle?.dataFormatString?.let { dataFormat ->
                CellExcelDataFormatAttribute(
                    dataFormat = dataFormat
                )
            } ?: CellExcelDataFormatAttribute("INVALID")
        }
    }
}

class PoiCellTypeHintAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): Attribute<*> {
        return api.xssfCell(Coordinates(tableId, select.rowIndex, select.columnIndex)).let {
            it?.cellType?.let { type ->
                TypeHintAttribute(
                        type = when (type) {
                            CellType.BOOLEAN -> ExcelTypeHints.BOOLEAN
                            CellType.NUMERIC -> ExcelTypeHints.NUMERIC
                            CellType.ERROR -> ExcelTypeHints.ERROR
                            CellType.FORMULA -> ExcelTypeHints.FORMULA
                            CellType.STRING -> ExcelTypeHints.STRING
                            else -> ExcelTypeHints.STRING
                        }
                )
            } ?: TypeHintAttribute(ExcelTypeHints.STRING)
        }
    }
}

class PoiCellCommentAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): Attribute<*> {
        return api.xssfCell(Coordinates(tableId, select.rowIndex, select.columnIndex))?.cellComment?.let {
            CellCommentAttribute(it.author, it.string.string)
        } ?: CellCommentAttribute()
    }
}


class PoiCellAlignmentAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): Attribute<*> {
        return api.xssfCell(Coordinates(tableId, select.rowIndex, select.columnIndex)).let {
            AlignmentAttribute(
                vertical = when (it?.cellStyle?.verticalAlignment) {
                    PoiVerticalAlignment.TOP -> DefaultVerticalAlignment.TOP
                    PoiVerticalAlignment.CENTER -> DefaultVerticalAlignment.MIDDLE
                    PoiVerticalAlignment.BOTTOM -> DefaultVerticalAlignment.BOTTOM
                    else -> DefaultVerticalAlignment.BOTTOM
                },
                horizontal = when (it?.cellStyle?.alignment) {
                    PoiHorizontalAlignment.LEFT -> DefaultHorizontalAlignment.LEFT
                    PoiHorizontalAlignment.RIGHT -> DefaultHorizontalAlignment.RIGHT
                    PoiHorizontalAlignment.CENTER -> DefaultHorizontalAlignment.CENTER
                    PoiHorizontalAlignment.JUSTIFY -> DefaultHorizontalAlignment.JUSTIFY
                    PoiHorizontalAlignment.FILL -> DefaultHorizontalAlignment.FILL
                    else -> DefaultHorizontalAlignment.LEFT
                }
            )
        }
    }
}

class PoiPrintingAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, SelectAll<Attribute<*>>>  {

    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: SelectAll<Attribute<*>>): Attribute<*> {
        api.workbook().let { wb ->
            val index = wb.getSheetIndex(tableId)
            val sheet = wb.getSheetAt(index)
            val address = wb.getPrintArea(index).let { CellRangeAddress.valueOf(it) }
            return PrintingAttribute(
                numberOfCopies = sheet.printSetup.copies,
                isDraft = sheet.printSetup.draft,
                blackAndWhite = sheet.printSetup.noColor,
                noOrientation=  sheet.printSetup.noOrientation,
                leftToRight = sheet.printSetup.leftToRight,
                printPageNumber = sheet.printSetup.usePage,
                firstPageNumber = sheet.printSetup.pageStart,
                paperSize = sheet.printSetup.paperSize,
                landscape = sheet.printSetup.landscape,
                headerMargin = sheet.printSetup.headerMargin,
                footerMargin = sheet.printSetup.footerMargin,
                fitHeight = sheet.printSetup.fitHeight,
                fitWidth = sheet.printSetup.fitWidth,
                firstPrintableColumn = address.firstColumn,
                lastPrintableColumn = address.lastColumn,
                firstPrintableRow = address.firstRow,
                lastPrintableRow = address.lastRow,
                footerCenter = sheet.footer.center,
                footerLeft = sheet.footer.left,
                footerRight = sheet.footer.right,
                headerCenter = sheet.header.center,
                headerLeft = sheet.header.left,
                headerRight = sheet.header.right,
            )
        }
    }
}

class PoiColumnWidthAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, ColumnPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: ColumnPosition): Attribute<*> {
        return api.workbook().getSheet(tableId)?.let {
            val autoSizing = it.isColumnTrackedForAutoSizing(select.columnIndex)
            val pxWidth = it.getColumnWidthInPixels(select.columnIndex).roundToInt()
            return if (autoSizing) {
                WidthAttribute(auto = true, Width.zero(UnitsOfMeasure.PX))
            } else {
                WidthAttribute(value = Width(pxWidth.toFloat(), UnitsOfMeasure.PX))
            }
        } ?: WidthAttribute()
    }
}

class PoiRowHeightAttributeResolver : AttributeResolver<ApachePoiRenderingContext, Attribute<*>, RowPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: RowPosition): Attribute<*>? {
        return api.workbook().xssfWorkbook.getSheet(tableId)?.getRow(select.rowIndex)?.let {
          HeightAttribute(Height(Units.pointsToPixel(it.heightInPoints.toDouble()).toFloat(), UnitsOfMeasure.PX))
        }
    }
}

