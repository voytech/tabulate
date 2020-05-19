package pl.voytech.exporter.core.model.hints.rendering

import pl.voytech.exporter.core.model.hints.CellHint

data class CellFontHint(
    val fontFamily: String,
    val fontSize: Int,
    val fontStyle: Int,
    val fontColor: Short
) : CellHint()
