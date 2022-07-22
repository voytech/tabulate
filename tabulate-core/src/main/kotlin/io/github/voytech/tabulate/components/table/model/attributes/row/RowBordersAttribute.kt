package io.github.voytech.tabulate.components.table.model.attributes.row

import io.github.voytech.tabulate.components.table.api.builder.RowAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.Color
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultBorderStyle
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.BorderStyle
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

data class RowBordersAttribute(
    val leftBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val leftBorderColor: Color? = null,
    val rightBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val rightBorderColor: Color? = null,
    val topBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val topBorderColor: Color? = null,
    val bottomBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val bottomBorderColor: Color? = null
) : RowAttribute<RowBordersAttribute>() {

    @TabulateMarker
    class Builder : RowAttributeBuilder<RowBordersAttribute>() {
        var leftBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var leftBorderColor: Color? by observable(null)
        var rightBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var rightBorderColor: Color? by observable(null)
        var topBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var topBorderColor: Color? by observable(null)
        var bottomBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var bottomBorderColor: Color? by observable(null)

        override fun provide(): RowBordersAttribute = RowBordersAttribute(
            leftBorderStyle,
            leftBorderColor,
            rightBorderStyle,
            rightBorderColor,
            topBorderStyle,
            topBorderColor,
            bottomBorderStyle,
            bottomBorderColor,
        )
    }

    override fun overrideWith(other: RowBordersAttribute): RowBordersAttribute = RowBordersAttribute(
        leftBorderStyle = takeIfChanged(other, RowBordersAttribute::leftBorderStyle),
        leftBorderColor = takeIfChanged(other, RowBordersAttribute::leftBorderColor),
        rightBorderStyle = takeIfChanged(other, RowBordersAttribute::rightBorderStyle),
        rightBorderColor = takeIfChanged(other, RowBordersAttribute::rightBorderColor),
        topBorderStyle = takeIfChanged(other, RowBordersAttribute::topBorderStyle),
        topBorderColor = takeIfChanged(other, RowBordersAttribute::topBorderColor),
        bottomBorderStyle = takeIfChanged(other, RowBordersAttribute::bottomBorderStyle),
        bottomBorderColor = takeIfChanged(other, RowBordersAttribute::bottomBorderColor),
    )

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }
}

fun <T: Any> RowLevelAttributesBuilderApi<T>.rowBorders(block: RowBordersAttribute.Builder.() -> Unit) =
    attribute(RowBordersAttribute.Builder().apply(block))
