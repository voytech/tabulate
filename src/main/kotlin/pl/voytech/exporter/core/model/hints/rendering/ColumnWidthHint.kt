package pl.voytech.exporter.core.model.hints.rendering

import pl.voytech.exporter.core.model.hints.ColumnHint

enum class LengthUnit {
    PIXEL
}

data class ColumnWidthHint(
    val width: Int,
    val unit: LengthUnit = LengthUnit.PIXEL
): ColumnHint()