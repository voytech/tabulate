package pl.voytech.exporter.core.model.hints.style

import pl.voytech.exporter.core.model.hints.CellHint

data class CellBackgroundHint(
    val color: Color
) : CellHint()
