package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TableLevelAttributesBuilderApi
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
    var rotation: Short? = 0
) : CellStyleAttribute<CellTextStylesAttribute>() {

    class Builder : CellAttributeBuilder<CellTextStylesAttribute> {
        var fontFamily: String? = null
        var fontSize: Int? = 10
        var weight: DefaultWeightStyle? = DefaultWeightStyle.NORMAL
        var italic: Boolean? = false
        var strikeout: Boolean? = false
        var underline: Boolean? = false
        var fontColor: Color? = null
        var ident: Short = 0
        var wrapText: Boolean = false
        var rotation: Short? = 0
        override fun build(): CellTextStylesAttribute =
            CellTextStylesAttribute(fontFamily, fontSize, weight, italic, strikeout, underline, fontColor, ident, wrapText, rotation)
    }

    override fun mergeWith(other: CellTextStylesAttribute): CellTextStylesAttribute = CellTextStylesAttribute(
        fontFamily = other.fontFamily ?: this.fontFamily,
        fontSize = other.fontSize ?: this.fontSize,
        weight = other.weight ?: this.weight,
        italic = other.italic ?: this.italic,
        strikeout = other.strikeout ?: this.strikeout,
        underline = other.underline ?: this.underline,
        fontColor = other.fontColor ?: this.fontColor,
        ident = other.ident ?: this.ident,
        wrapText = other.wrapText ?: this.wrapText,
        rotation = other.rotation ?: this.rotation
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