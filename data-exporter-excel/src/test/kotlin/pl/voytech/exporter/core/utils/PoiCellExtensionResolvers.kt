package pl.voytech.exporter.core.utils

import org.apache.poi.ss.usermodel.Font
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.CellFontExtension
import pl.voytech.exporter.core.model.extension.style.Color
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI
import pl.voytech.exporter.core.testutils.ExtensionResolver
import pl.voytech.exporter.impl.template.excel.PoiUtils
import pl.voytech.exporter.impl.template.excel.PoiWrapper

class PoiCellFontExtensionResolver : ExtensionResolver<SXSSFWorkbook> {
    override fun resolve(api: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates): CellExtension {
        return PoiWrapper.xssfCell(api,coordinates).let {
            CellFontExtension(
                fontFamily = it?.cellStyle?.font?.fontName,
                fontSize = it?.cellStyle?.font?.fontHeight?.toInt()?.let { size -> PoiUtils.pixelsFromHeight(size) },
                fontColor = it?.cellStyle?.font?.xssfColor?.let { color -> Color(color.rgb[0].toInt(),color.rgb[1].toInt(),color.rgb[2].toInt()) },
                weight = it?.cellStyle?.font?.bold?.let { bold -> if (bold) { WeightStyle.BOLD } else { WeightStyle.NORMAL} },
                strikeout = it?.cellStyle?.font?.strikeout,
                underline = it?.cellStyle?.font?.underline == Font.U_SINGLE,
                italic = it?.cellStyle?.font?.italic
            )
        }
    }
}