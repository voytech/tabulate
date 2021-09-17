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
    class Builder : ColumnAttributeBuilder() {
        var auto: Boolean? by observable(false)
        var px: Int by observable(-1)
        var unit: LengthUnit by observable(LengthUnit.PIXEL)
        override fun provide(): ColumnAttribute<ColumnWidthAttribute> = ColumnWidthAttribute(auto, px, unit)
    }

    override fun mergeWith(other: ColumnWidthAttribute): ColumnWidthAttribute = ColumnWidthAttribute(
        auto = takeIfChanged(other, ColumnWidthAttribute::auto),
        px = takeIfChanged(other, ColumnWidthAttribute::px),
        unit = takeIfChanged(other, ColumnWidthAttribute::unit),
    )
}

fun <T> ColumnLevelAttributesBuilderApi<T>.width(block: ColumnWidthAttribute.Builder.() -> Unit) =
    attribute(ColumnWidthAttribute.Builder().apply(block).build())

