package io.github.voytech.tabulate.model.attributes.column

import io.github.voytech.tabulate.api.builder.ColumnAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TabulateMarker
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

    @TabulateMarker
    class Builder : ColumnAttributeBuilder {
        var auto: Boolean? = false
        var px: Int = -1
        var unit: LengthUnit = LengthUnit.PIXEL
        override fun build(): ColumnAttribute<ColumnWidthAttribute> = ColumnWidthAttribute(auto, px, unit)
    }

    override fun mergeWith(other: ColumnWidthAttribute): ColumnWidthAttribute = other.copy(auto = other.auto ?: this.auto)
}

fun <T> ColumnLevelAttributesBuilderApi<T>.width(block: ColumnWidthAttribute.Builder.() -> Unit) =
    attribute(ColumnWidthAttribute.Builder().apply(block).build())

