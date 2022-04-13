package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.*
import io.github.voytech.tabulate.model.attributes.Color
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultBorderStyle
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.BorderStyle

data class CellBordersAttribute(
    val leftBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val leftBorderColor: Color? = null,
    val rightBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val rightBorderColor: Color? = null,
    val topBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val topBorderColor: Color? = null,
    val bottomBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    val bottomBorderColor: Color? = null
) : CellStyleAttribute<CellBordersAttribute>() {

    @TabulateMarker
    class Builder : CellAttributeBuilder<CellBordersAttribute>() {
        var leftBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var leftBorderColor: Color? by observable(null)
        var rightBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var rightBorderColor: Color? by observable(null)
        var topBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var topBorderColor: Color? by observable(null)
        var bottomBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var bottomBorderColor: Color? by observable(null)

        override fun provide(): CellBordersAttribute = CellBordersAttribute(
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

    override fun overrideWith(other: CellBordersAttribute): CellBordersAttribute = CellBordersAttribute(
        leftBorderStyle = takeIfChanged(other, CellBordersAttribute::leftBorderStyle),
        leftBorderColor = takeIfChanged(other, CellBordersAttribute::leftBorderColor),
        rightBorderStyle = takeIfChanged(other, CellBordersAttribute::rightBorderStyle),
        rightBorderColor = takeIfChanged(other, CellBordersAttribute::rightBorderColor),
        topBorderStyle = takeIfChanged(other, CellBordersAttribute::topBorderStyle),
        topBorderColor = takeIfChanged(other, CellBordersAttribute::topBorderColor),
        bottomBorderStyle = takeIfChanged(other, CellBordersAttribute::bottomBorderStyle),
        bottomBorderColor = takeIfChanged(other, CellBordersAttribute::bottomBorderColor),
    )

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }
}

fun <T> CellLevelAttributesBuilderApi<T>.borders(block: CellBordersAttribute.Builder.() -> Unit) =
    attribute(CellBordersAttribute.Builder().apply(block))

fun <T> ColumnLevelAttributesBuilderApi<T>.borders(block: CellBordersAttribute.Builder.() -> Unit) =
    attribute(CellBordersAttribute.Builder().apply(block))

fun <T> RowLevelAttributesBuilderApi<T>.borders(block: CellBordersAttribute.Builder.() -> Unit) =
    attribute(CellBordersAttribute.Builder().apply(block))

fun <T> TableLevelAttributesBuilderApi<T>.borders(block: CellBordersAttribute.Builder.() -> Unit) =
    attribute(CellBordersAttribute.Builder().apply(block))
