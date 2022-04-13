package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.*
import io.github.voytech.tabulate.model.attributes.Color
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultWeightStyle

data class CellTextStylesAttribute(
    val fontFamily: String? = null,
    val fontSize: Int? = 10,
    val weight: DefaultWeightStyle? = DefaultWeightStyle.NORMAL,
    val italic: Boolean? = false,
    val strikeout: Boolean? = false,
    val underline: Boolean? = false,
    val fontColor: Color? = null,
    val ident: Short? = 0,
    val wrapText: Boolean? = false,
    var rotation: Short? = 0,
) : CellStyleAttribute<CellTextStylesAttribute>() {

    @TabulateMarker
    class Builder : CellAttributeBuilder<CellTextStylesAttribute>() {
        var fontFamily: String? by observable(null)
        var fontSize: Int? by observable(10)
        var weight: DefaultWeightStyle? by observable(DefaultWeightStyle.NORMAL)
        var italic: Boolean? by observable(false)
        var strikeout: Boolean? by observable(false)
        var underline: Boolean? by observable(false)
        var fontColor: Color? by observable(null)
        var ident: Short by observable(0)
        var wrapText: Boolean by observable(false)
        var rotation: Short? by observable(0)

        override fun provide(): CellTextStylesAttribute =
            CellTextStylesAttribute(
                fontFamily, fontSize, weight, italic, strikeout, underline, fontColor, ident, wrapText, rotation
            )
    }

    override fun overrideWith(other: CellTextStylesAttribute): CellTextStylesAttribute = CellTextStylesAttribute(
        fontFamily = takeIfChanged(other, CellTextStylesAttribute::fontFamily),
        fontSize = takeIfChanged(other, CellTextStylesAttribute::fontSize),
        weight = takeIfChanged(other, CellTextStylesAttribute::weight),
        italic = takeIfChanged(other, CellTextStylesAttribute::italic),
        strikeout = takeIfChanged(other, CellTextStylesAttribute::strikeout),
        underline = takeIfChanged(other, CellTextStylesAttribute::underline),
        fontColor = takeIfChanged(other, CellTextStylesAttribute::fontColor),
        ident = takeIfChanged(other, CellTextStylesAttribute::ident),
        wrapText = takeIfChanged(other, CellTextStylesAttribute::wrapText),
        rotation = takeIfChanged(other, CellTextStylesAttribute::rotation),
    )

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }
}

fun <T> CellLevelAttributesBuilderApi<T>.text(block: CellTextStylesAttribute.Builder.() -> Unit) =
    attribute(CellTextStylesAttribute.Builder().apply(block))

fun <T> ColumnLevelAttributesBuilderApi<T>.text(block: CellTextStylesAttribute.Builder.() -> Unit) =
    attribute(CellTextStylesAttribute.Builder().apply(block))

fun <T> RowLevelAttributesBuilderApi<T>.text(block: CellTextStylesAttribute.Builder.() -> Unit) =
    attribute(CellTextStylesAttribute.Builder().apply(block))

fun <T> TableLevelAttributesBuilderApi<T>.text(block: CellTextStylesAttribute.Builder.() -> Unit) =
    attribute(CellTextStylesAttribute.Builder().apply(block))