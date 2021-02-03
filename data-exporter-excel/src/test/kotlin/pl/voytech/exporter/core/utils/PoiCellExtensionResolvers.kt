package pl.voytech.exporter.core.utils

import org.apache.poi.ss.usermodel.Font
import org.apache.poi.xssf.usermodel.XSSFColor
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.style.*
import pl.voytech.exporter.core.model.attributes.style.enums.BorderStyle
import pl.voytech.exporter.core.model.attributes.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.WeightStyle
import pl.voytech.exporter.core.template.context.Coordinates
import pl.voytech.exporter.impl.template.excel.CellExcelDataFormatAttribute
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiUtils
import pl.voytech.exporter.testutils.AttributeResolver
import org.apache.poi.ss.usermodel.BorderStyle as PoiBorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment as PoiHorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment as PoiVerticalAlignment

private fun parseColor(xssfColor: XSSFColor) =
    Color(xssfColor.rgb[0].toInt().and(0xFF), xssfColor.rgb[1].toInt().and(0xFF), xssfColor.rgb[2].toInt().and(0xFF))

class PoiCellFontAttributeResolver : AttributeResolver<ApachePoiExcelFacade> {
    override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellAttribute {
        return api.xssfCell(coordinates).let {
            CellFontAttribute(
                fontFamily = it?.cellStyle?.font?.fontName,
                fontSize = it?.cellStyle?.font?.fontHeight?.toInt()?.let { size -> ApachePoiUtils.pixelsFromHeight(size) },
                fontColor = it?.cellStyle?.font?.xssfColor?.let { color -> parseColor(color) },
                weight = it?.cellStyle?.font?.bold?.let { bold ->
                    if (bold) {
                        WeightStyle.BOLD
                    } else {
                        WeightStyle.NORMAL
                    }
                },
                strikeout = it?.cellStyle?.font?.strikeout,
                underline = it?.cellStyle?.font?.underline == Font.U_SINGLE,
                italic = it?.cellStyle?.font?.italic
            )
        }
    }
}

class PoiCellBackgroundAttributeResolver : AttributeResolver<ApachePoiExcelFacade> {
    override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellAttribute {
        return api.xssfCell(coordinates).let {
            CellBackgroundAttribute(
                color = it?.cellStyle?.fillForegroundXSSFColor?.let { color -> parseColor(color) } ?: Color(-1, -1, -1)
            )
        }
    }
}

class PoiCellBordersAttributeResolver : AttributeResolver<ApachePoiExcelFacade> {
    override fun resolve(api: ApachePoiExcelFacade, coordinates: Coordinates): CellAttribute {
        val fromPoiStyle = { style: PoiBorderStyle ->
            when (style) {
                PoiBorderStyle.DASHED -> BorderStyle.DASHED
                PoiBorderStyle.DOTTED -> BorderStyle.DOTTED
                PoiBorderStyle.THIN -> BorderStyle.SOLID
                else -> BorderStyle.NONE
            }
        }
        return api.xssfCell(coordinates).let {
            CellBordersAttribute(
                leftBorderStyle = it?.cellStyle?.borderLeft?.let { border -> fromPoiStyle(border) },
                leftBorderColor = it?.cellStyle?.leftBorderXSSFColor?.let { color -> parseColor(color) },
                rightBorderStyle = it?.cellStyle?.borderRight?.let { border -> fromPoiStyle(border) },
                rightBorderColor = it?.cellStyle?.rightBorderXSSFColor?.let { color -> parseColor(color) },
                topBorderStyle = it?.cellStyle?.borderTop?.let { border -> fromPoiStyle(border) },
                topBorderColor = it?.cellStyle?.topBorderXSSFColor?.let { color -> parseColor(color) },
                bottomBorderStyle = it?.cellStyle?.borderBottom?.let { border -> fromPoiStyle(border) },
                bottomBorderColor = it?.cellStyle?.bottomBorderXSSFColor?.let { color -> parseColor(color) }
            )
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
                    PoiVerticalAlignment.TOP -> VerticalAlignment.TOP
                    PoiVerticalAlignment.CENTER -> VerticalAlignment.MIDDLE
                    PoiVerticalAlignment.BOTTOM -> VerticalAlignment.BOTTOM
                    else -> null
                },
                horizontal = when (it?.cellStyle?.alignment) {
                    PoiHorizontalAlignment.LEFT -> HorizontalAlignment.LEFT
                    PoiHorizontalAlignment.RIGHT -> HorizontalAlignment.RIGHT
                    PoiHorizontalAlignment.CENTER -> HorizontalAlignment.CENTER
                    PoiHorizontalAlignment.JUSTIFY -> HorizontalAlignment.JUSTIFY
                    else -> null
                }
            )
        }
    }
}
