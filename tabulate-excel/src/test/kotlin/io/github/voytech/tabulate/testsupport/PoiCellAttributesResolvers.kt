package io.github.voytech.tabulate.testsupport

import io.github.voytech.tabulate.excel.model.ExcelBorderStyle
import io.github.voytech.tabulate.excel.model.ExcelCellFills
import io.github.voytech.tabulate.excel.model.ExcelTypeHints
import io.github.voytech.tabulate.excel.model.attributes.CellCommentAttribute
import io.github.voytech.tabulate.excel.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.excel.model.attributes.PrintingAttribute
import io.github.voytech.tabulate.excel.template.ApachePoiRenderingContext
import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.model.attributes.cell.*
import io.github.voytech.tabulate.model.attributes.cell.enums.*
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.BorderStyle
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.CellFill
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.template.operations.Coordinates
import io.github.voytech.tabulate.test.*
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.util.Units
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.ss.usermodel.BorderStyle as PoiBorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment as PoiHorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment as PoiVerticalAlignment

private fun parseColor(xssfColor: XSSFColor) =
    Color(
        xssfColor.rgb[0].toInt().and(0xFF),
        xssfColor.rgb[1].toInt().and(0xFF),
        xssfColor.rgb[2].toInt().and(0xFF)
    )

class PoiCellFontAttributeResolver : AttributeResolver<ApachePoiRenderingContext,CellAttribute<*>, CellPosition> {

    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): CellAttribute<*> {
        return api.xssfCell(Coordinates(tableId, select.rowIndex, select.columnIndex)).let { cell ->
            CellTextStylesAttribute(
                fontFamily = cell?.cellStyle?.font?.fontName,
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

class PoiCellBackgroundAttributeResolver : AttributeResolver<ApachePoiRenderingContext, CellAttribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): CellAttribute<*> {
        return with(api.xssfCell(Coordinates(tableId,select.rowIndex,select.columnIndex))?.cellStyle) {
            if (this != null) {
                CellBackgroundAttribute(
                    color = fillForegroundXSSFColor?.let { parseColor(it) },
                    fill = fillPattern.let {
                        when (it) {
                            FillPatternType.BIG_SPOTS -> DefaultCellFill.LARGE_SPOTS
                            FillPatternType.SQUARES -> DefaultCellFill.SQUARES
                            FillPatternType.FINE_DOTS -> DefaultCellFill.SMALL_DOTS
                            FillPatternType.ALT_BARS -> DefaultCellFill.WIDE_DOTS
                            FillPatternType.DIAMONDS -> DefaultCellFill.DIAMONDS
                            FillPatternType.BRICKS -> DefaultCellFill.BRICKS
                            FillPatternType.SOLID_FOREGROUND -> DefaultCellFill.SOLID
                            else -> resolveCellFillPattern(it)
                        }
                    } ?: DefaultCellFill.SOLID
                )
            } else CellBackgroundAttribute()
        }
    }

    private fun resolveCellFillPattern(fillPattern: FillPatternType?): CellFill? {
        return try {
            if (fillPattern != null) ExcelCellFills.valueOf(fillPattern.name) else DefaultCellFill.SOLID
        } catch (e: IllegalArgumentException) {
            DefaultCellFill.SOLID
        }
    }
}

class PoiCellBordersAttributeResolver : AttributeResolver<ApachePoiRenderingContext, CellAttribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): CellAttribute<*> {

        return api.xssfCell(Coordinates(tableId,select.rowIndex,select.columnIndex)).let { cell ->
            CellBordersAttribute(
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

class PoiCellDataFormatAttributeResolver : AttributeResolver<ApachePoiRenderingContext, CellAttribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): CellAttribute<*> {
        return api.xssfCell(Coordinates(tableId,select.rowIndex,select.columnIndex)).let {
            it?.cellStyle?.dataFormatString?.let { dataFormat ->
                CellExcelDataFormatAttribute(
                    dataFormat = dataFormat
                )
            } ?: CellExcelDataFormatAttribute("INVALID")
        }
    }
}

class PoiCellTypeHintAttributeResolver : AttributeResolver<ApachePoiRenderingContext, CellAttribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): CellAttribute<*> {
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

class PoiCellCommentAttributeResolver : AttributeResolver<ApachePoiRenderingContext, CellAttribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): CellAttribute<*> {
        return api.xssfCell(Coordinates(tableId, select.rowIndex, select.columnIndex))?.cellComment?.let {
            CellCommentAttribute(it.author, it.string.string)
        } ?: CellCommentAttribute()
    }
}


class PoiCellAlignmentAttributeResolver : AttributeResolver<ApachePoiRenderingContext, CellAttribute<*>, CellPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: CellPosition): CellAttribute<*> {
        return api.xssfCell(Coordinates(tableId, select.rowIndex, select.columnIndex)).let {
            CellAlignmentAttribute(
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

class PoiPrintingAttributeResolver : AttributeResolver<ApachePoiRenderingContext, TableAttribute<*>, SelectAll<TableAttribute<*>>>  {

    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: SelectAll<TableAttribute<*>>): TableAttribute<*> {
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

class PoiColumnWidthAttributeResolver : AttributeResolver<ApachePoiRenderingContext, ColumnAttribute<*>, ColumnPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: ColumnPosition): ColumnAttribute<*> {
        return api.workbook().getSheet(tableId)?.let {
            val autoSizing = it.isColumnTrackedForAutoSizing(select.columnIndex)
            val pxWidth = it.getColumnWidthInPixels(select.columnIndex)
            return if (autoSizing) {
                ColumnWidthAttribute(auto = true)
            } else {
                ColumnWidthAttribute(px = pxWidth.toInt())
            }
        } ?: ColumnWidthAttribute()
    }
}

class PoiRowHeightAttributeResolver : AttributeResolver<ApachePoiRenderingContext, RowAttribute<*>, RowPosition> {
    override fun resolve(api: ApachePoiRenderingContext, tableId: String, select: RowPosition): RowAttribute<*>? {
        return api.workbook().xssfWorkbook.getSheet(tableId)?.getRow(select.rowIndex)?.let {
          RowHeightAttribute(Units.pointsToPixel(it.heightInPoints.toDouble()))
        }
    }
}

