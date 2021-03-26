package pl.voytech.exporter.core.model.attributes.column

import pl.voytech.exporter.core.api.builder.ColumnAttributeBuilder
import pl.voytech.exporter.core.model.attributes.ColumnAttribute

enum class LengthUnit {
    PIXEL, CHARACTER
}

data class ColumnWidthAttribute(
    val auto: Boolean? = false,
    val width: Int = -1,
    val unit: LengthUnit = LengthUnit.PIXEL
) : ColumnAttribute<ColumnWidthAttribute>() {
    override fun beforeFirstRow() = true
    override fun afterLastRow() = true

    class Builder : ColumnAttributeBuilder {
        var auto: Boolean? = false
        var width: Int = -1
        var unit: LengthUnit = LengthUnit.PIXEL
        override fun build(): ColumnAttribute<ColumnWidthAttribute> = ColumnWidthAttribute(auto, width, unit)
    }

    override fun mergeWith(other: ColumnWidthAttribute): ColumnWidthAttribute = other.copy(auto = other.auto ?: this.auto)
}

fun width(block: ColumnWidthAttribute.Builder.() -> Unit): ColumnAttribute<ColumnWidthAttribute> = ColumnWidthAttribute.Builder().apply(block).build()

