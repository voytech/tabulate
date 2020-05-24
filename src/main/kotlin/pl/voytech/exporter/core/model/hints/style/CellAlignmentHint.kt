package pl.voytech.exporter.core.model.hints.style

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.hints.style.enums.VerticalAlignment

data class CellAlignmentHint(
    val vertical: VerticalAlignment? = null,
    val horizontal: HorizontalAlignment? = null
): CellHint()
