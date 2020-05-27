package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.model.extension.ColumnExtension

enum class LengthUnit {
    PIXEL, CHARACTER
}

data class ColumnWidthExtension(
    val width: Int,
    val unit: LengthUnit = LengthUnit.PIXEL
): ColumnExtension()