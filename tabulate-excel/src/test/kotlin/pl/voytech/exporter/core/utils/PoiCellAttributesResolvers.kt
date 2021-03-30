package pl.voytech.exporter.core.utils

import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.xssf.usermodel.XSSFColor
import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.model.attributes.cell.*
import pl.voytech.exporter.core.model.attributes.cell.enums.*
import pl.voytech.exporter.core.model.attributes.cell.enums.contract.BorderStyle
import pl.voytech.exporter.core.template.context.Coordinates
import pl.voytech.exporter.impl.template.excel.CellExcelDataFormatAttribute
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiUtils
import pl.voytech.exporter.impl.template.model.ExcelBorderStyle
import pl.voytech.exporter.impl.template.model.ExcelCellFills
import pl.voytech.exporter.testutils.AttributeResolver
import org.apache.poi.ss.usermodel.BorderStyle as PoiBorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment as PoiHorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment as PoiVerticalAlignment

private fun parseColor(xssfColor: XSSFColor) =
    Color(
        xssfColor.rgb[0].toInt().and(0xFF),
        xssfColor.rgb[1].toInt().and(0xFF),
        xssfColor.rgb[2].toInt().and(0xFF)
    )

class PoiCellFontAttributeResolver : AttributeResolver<ApachePoiExcelFacade> {
    override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellAttribute {
        return api.xssfCell(coordinates).let { cell ->
            CellTextStylesAttribute(
                fontFamily = cell?.cellStyle?.font?.fontName,
                fontSize = cell?.cellStyle?.font?.fontHeight?.toInt()?.let { ApachePoiUtils.pixelsFromHeight(it) },
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

class PoiCellBackgroundAttributeResolver : AttributeResolver<ApachePoiExcelFacade> {
    override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellAttribute {
        return with(api.xssfCell(coordinates)?.cellStyle) {
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
                    }
                )
            } else CellBackgroundAttribute()
        }
    }

    private fun resolveCellFillPattern(fillPattern: FillPatternType?): ExcelCellFills? {
        return try {
            if (fillPattern != null) ExcelCellFills.valueOf(fillPattern.name) else null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

class PoiCellBordersAttributeResolver : AttributeResolver<ApachePoiExcelFacade> {
    override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellAttribute {

        return api.xssfCell(coordinates).let { cell ->
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

class PoiCellDataFormatAttributeResolver : AttributeResolver<ApachePoiExcelFacade> {
    override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellAttribute {
        return api.xssfCell(coordinates).let {
            it?.cellStyle?.dataFormatString?.let { dataFormat ->
                CellExcelDataFormatAttribute(
                    dataFormat = dataFormat
                )
            } ?: CellExcelDataFormatAttribute("INVALID")
        }
    }
}

class PoiCellAlignmentAttributeResolver : AttributeResolver<ApachePoiExcelFacade> {
    override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellAttribute {
        return api.xssfCell(coordinates).let {
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
