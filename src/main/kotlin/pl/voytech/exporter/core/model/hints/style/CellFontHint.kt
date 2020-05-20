package pl.voytech.exporter.core.model.hints.style

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.style.enums.WeightStyle

data class CellFontHint(
    val fontFamily: String?,
    val fontSize: Int? = 12,
    val weight: WeightStyle? = WeightStyle.NORMAL,
    val italic: Boolean? = false,
    val strikeout: Boolean? = false,
    val underline: Boolean? = false,
    val fontColor: Color? = Color(0,0,0)
) : CellHint()
