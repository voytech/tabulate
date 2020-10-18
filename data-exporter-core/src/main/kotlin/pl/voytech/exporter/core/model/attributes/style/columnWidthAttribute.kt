package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.ColumnAttributeBuilder
import pl.voytech.exporter.core.model.attributes.ColumnAttribute

enum class LengthUnit {
    PIXEL, CHARACTER
}

data class ColumnWidthAttribute(
    val auto: Boolean? = false,
    val width: Int = -1,
    val unit: LengthUnit = LengthUnit.PIXEL
) : ColumnAttribute() {
    override fun beforeFirstRow() = true
    override fun afterLastRow() = true

    class Builder : ColumnAttributeBuilder {
        var auto: Boolean? = false
        var width: Int = -1
        var unit: LengthUnit = LengthUnit.PIXEL
        override fun build(): ColumnAttribute = ColumnWidthAttribute(auto, width, unit)
    }
}

fun width(block: ColumnWidthAttribute.Builder.() -> Unit): ColumnAttribute = ColumnWidthAttribute.Builder().apply(block).build()

