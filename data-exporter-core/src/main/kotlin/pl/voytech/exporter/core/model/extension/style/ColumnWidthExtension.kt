package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.model.extension.ColumnExtension

enum class LengthUnit {
    PIXEL, CHARACTER
}

data class ColumnWidthExtension(
    val auto: Boolean? = false,
    val width: Int = -1,
    val unit: LengthUnit = LengthUnit.PIXEL
) : ColumnExtension() {
    override fun beforeFirstRow() = false
    override fun afterLastRow() = true
}