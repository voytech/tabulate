package io.github.voytech.tabulate.model.attributes.column

import io.github.voytech.tabulate.api.builder.ColumnAttributeBuilder
import io.github.voytech.tabulate.model.attributes.ColumnAttribute

enum class LengthUnit {
    PIXEL, CHARACTER
}

data class ColumnWidthAttribute(
    val auto: Boolean? = false,
    val px: Int = -1,
    val unit: LengthUnit = LengthUnit.PIXEL
) : ColumnAttribute<ColumnWidthAttribute>() {
    override fun beforeFirstRow() = true
    override fun afterLastRow() = true

    class Builder : ColumnAttributeBuilder {
        var auto: Boolean? = false
        var px: Int = -1
        var unit: LengthUnit = LengthUnit.PIXEL
        override fun build(): ColumnAttribute<ColumnWidthAttribute> = ColumnWidthAttribute(auto, px, unit)
    }

    override fun mergeWith(other: ColumnWidthAttribute): ColumnWidthAttribute = other.copy(auto = other.auto ?: this.auto)
}

fun width(block: ColumnWidthAttribute.Builder.() -> Unit): ColumnAttribute<ColumnWidthAttribute> = ColumnWidthAttribute.Builder().apply(block).build()

