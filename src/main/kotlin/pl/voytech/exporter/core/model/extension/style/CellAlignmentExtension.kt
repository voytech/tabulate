package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.VerticalAlignment

data class CellAlignmentExtension(
    val vertical: VerticalAlignment? = null,
    val horizontal: HorizontalAlignment? = null
): CellExtension()
