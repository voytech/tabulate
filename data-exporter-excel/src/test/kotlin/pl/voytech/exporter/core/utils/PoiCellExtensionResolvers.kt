package pl.voytech.exporter.core.utils

import org.apache.poi.ss.usermodel.Font
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFColor
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.*
import pl.voytech.exporter.core.model.extension.style.enums.BorderStyle
import pl.voytech.exporter.core.model.extension.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI
import pl.voytech.exporter.impl.template.excel.CellExcelDataFormatExtension
import pl.voytech.exporter.impl.template.excel.PoiUtils
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper
import pl.voytech.exporter.testutils.ExtensionResolver
import org.apache.poi.ss.usermodel.BorderStyle as PoiBorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment as PoiHorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment as PoiVerticalAlignment

private fun parseColor(xssfColor: XSSFColor) =
    Color(xssfColor.rgb[0].toInt(), xssfColor.rgb[1].toInt(), xssfColor.rgb[2].toInt())

class PoiCellFontExtensionResolver : ExtensionResolver<SXSSFWorkbook> {
    override fun resolve(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellExtension {
        return SXSSFWrapper.xssfCell(api, coordinates).let {
            CellFontExtension(
                fontFamily = it?.cellStyle?.font?.fontName,
                fontSize = it?.cellStyle?.font?.fontHeight?.toInt()?.let { size -> PoiUtils.pixelsFromHeight(size) },
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

class PoiCellBackgroundExtensionResolver : ExtensionResolver<SXSSFWorkbook> {
    override fun resolve(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellExtension {
        return SXSSFWrapper.xssfCell(api, coordinates).let {
            CellBackgroundExtension(
                color = it?.cellStyle?.fillForegroundXSSFColor?.let { color -> parseColor(color) } ?: Color(-1, -1, -1)
            )
        }
    }
}

class PoiCellBordersExtensionResolver : ExtensionResolver<SXSSFWorkbook> {
    override fun resolve(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellExtension {
        val fromPoiStyle = { style: PoiBorderStyle ->
            when (style) {
                PoiBorderStyle.DASHED -> BorderStyle.DASHED
                PoiBorderStyle.DOTTED -> BorderStyle.DOTTED
                PoiBorderStyle.THIN -> BorderStyle.SOLID
                else -> BorderStyle.NONE
            }
        }
        return SXSSFWrapper.xssfCell(api, coordinates).let {
            CellBordersExtension(
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

class PoiCellDataFormatExtensionResolver : ExtensionResolver<SXSSFWorkbook> {
    override fun resolve(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellExtension {
        return SXSSFWrapper.xssfCell(api, coordinates).let {
            it?.cellStyle?.dataFormatString?.let { dataFormat ->
                CellExcelDataFormatExtension(
                    dataFormat = dataFormat
                )
            } ?: CellExcelDataFormatExtension("INVALID")
        }
    }
}

class PoiCellAlignmentExtensionResolver : ExtensionResolver<SXSSFWorkbook> {
    override fun resolve(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellExtension {
        return SXSSFWrapper.xssfCell(api, coordinates).let {
            CellAlignmentExtension(
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