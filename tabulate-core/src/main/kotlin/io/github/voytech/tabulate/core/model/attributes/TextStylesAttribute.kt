package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.core.model.text.Font
import io.github.voytech.tabulate.core.template.operation.AttributedContext

data class TextStylesAttribute(
    val fontFamily: Font? = null,
    val fontSize: Int? = 10,
    val weight: DefaultWeightStyle? = DefaultWeightStyle.NORMAL,
    val italic: Boolean? = false,
    val strikeout: Boolean? = false,
    val underline: Boolean? = false,
    val fontColor: Color? = null,
    val ident: Short? = 0,
    val wrapText: Boolean? = false,
    var rotation: Short? = 0,
) : Attribute<TextStylesAttribute>() {

    @TabulateMarker
    class Builder(target: Class<out AttributedContext>) : AttributeBuilder<TextStylesAttribute>(target) {
        var fontFamily: Font? by observable(null)
        var fontSize: Int? by observable(10)
        var weight: DefaultWeightStyle? by observable(DefaultWeightStyle.NORMAL)
        var italic: Boolean? by observable(false)
        var strikeout: Boolean? by observable(false)
        var underline: Boolean? by observable(false)
        var fontColor: Color? by observable(null)
        var ident: Short by observable(0)
        var wrapText: Boolean by observable(false)
        var rotation: Short? by observable(0)

        override fun provide(): TextStylesAttribute =
            TextStylesAttribute(
                fontFamily, fontSize, weight, italic, strikeout, underline, fontColor, ident, wrapText, rotation
            )
    }

    override fun overrideWith(other: TextStylesAttribute): TextStylesAttribute = TextStylesAttribute(
        fontFamily = takeIfChanged(other, TextStylesAttribute::fontFamily),
        fontSize = takeIfChanged(other, TextStylesAttribute::fontSize),
        weight = takeIfChanged(other, TextStylesAttribute::weight),
        italic = takeIfChanged(other, TextStylesAttribute::italic),
        strikeout = takeIfChanged(other, TextStylesAttribute::strikeout),
        underline = takeIfChanged(other, TextStylesAttribute::underline),
        fontColor = takeIfChanged(other, TextStylesAttribute::fontColor),
        ident = takeIfChanged(other, TextStylesAttribute::ident),
        wrapText = takeIfChanged(other, TextStylesAttribute::wrapText),
        rotation = takeIfChanged(other, TextStylesAttribute::rotation),
    )

    companion object {
        @JvmStatic
        fun  builder(target: Class<out AttributedContext>) : Builder =
            Builder(target)

        @JvmStatic
        inline fun <reified AC: AttributedContext> builder() : Builder =
            Builder(AC::class.java)
    }
}
