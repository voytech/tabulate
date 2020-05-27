package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle

data class CellFontExtension(
    val fontFamily: String?,
    val fontSize: Int? = 12,
    val weight: WeightStyle? = WeightStyle.NORMAL,
    val italic: Boolean? = false,
    val strikeout: Boolean? = false,
    val underline: Boolean? = false,
    val fontColor: Color? = Color(0,0,0)
) : CellExtension()
