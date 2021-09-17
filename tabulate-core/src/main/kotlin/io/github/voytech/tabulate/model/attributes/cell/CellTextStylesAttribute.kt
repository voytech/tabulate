package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.*
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
        var underline: Boolean?by observable(false)
        var fontColor: Color? by observable(null)
        var ident: Short by observable(0)
        var wrapText: Boolean by observable(false)
        var rotation: Short? by observable(0)

        override fun provide(): CellTextStylesAttribute =
            CellTextStylesAttribute(
                fontFamily, fontSize, weight, italic, strikeout, underline, fontColor, ident, wrapText, rotation
            )
    }

    override fun mergeWith(other: CellTextStylesAttribute): CellTextStylesAttribute = CellTextStylesAttribute(
        fontFamily = other.takeIfChangedOrElse(other::fontFamily, ::fontFamily),
        fontSize = other.takeIfChangedOrElse(other::fontSize, ::fontSize),
        weight = other.takeIfChangedOrElse(other::weight, ::weight),
        italic = other.takeIfChangedOrElse(other::italic, ::italic),
        strikeout = other.takeIfChangedOrElse(other::strikeout, ::strikeout),
        underline = other.takeIfChangedOrElse(other::underline, ::underline),
        fontColor = other. takeIfChangedOrElse(other::fontColor, ::fontColor),
        ident = other.takeIfChangedOrElse(other::ident, ::ident),
        wrapText = other.takeIfChangedOrElse(other::wrapText, ::wrapText),
        rotation = other.takeIfChangedOrElse(other::rotation, ::rotation),
    )
}

fun <T> CellLevelAttributesBuilderApi<T>.text(block: CellTextStylesAttribute.Builder.() -> Unit) =
    attribute(CellTextStylesAttribute.Builder().apply(block).build())

fun <T> ColumnLevelAttributesBuilderApi<T>.text(block: CellTextStylesAttribute.Builder.() -> Unit) =
    attribute(CellTextStylesAttribute.Builder().apply(block).build())

fun <T> RowLevelAttributesBuilderApi<T>.text(block: CellTextStylesAttribute.Builder.() -> Unit) =
    attribute(CellTextStylesAttribute.Builder().apply(block).build())

fun <T> TableLevelAttributesBuilderApi<T>.text(block: CellTextStylesAttribute.Builder.() -> Unit) =
    attribute(CellTextStylesAttribute.Builder().apply(block).build())