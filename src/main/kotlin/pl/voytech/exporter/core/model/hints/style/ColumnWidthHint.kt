package pl.voytech.exporter.core.model.hints.style

import pl.voytech.exporter.core.model.hints.ColumnHint

enum class LengthUnit {
    PIXEL, CHARACTER
}

data class ColumnWidthHint(
    val width: Int,
    val unit: LengthUnit = LengthUnit.PIXEL
): ColumnHint()