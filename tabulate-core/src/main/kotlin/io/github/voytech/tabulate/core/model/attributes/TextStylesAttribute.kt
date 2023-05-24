package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.color.DefaultColorWords
import io.github.voytech.tabulate.core.model.text.*

data class TextStylesAttribute(
    val fontFamily: Font? = null,
    val fontSize: Int? = 10,
    val weight: WeightStyle? = DefaultWeightStyle.NORMAL,
    val italic: Boolean? = false,
    val strikeout: Boolean? = false,
    val underline: Boolean? = false,
    val fontColor: Color? = null,
    val ident: Short? = 0,    //TODO move it to another attribute
    @Deprecated("Currently for backward compatibility", replaceWith = ReplaceWith("textWrap"))
    val wrapText: Boolean? = false,
    val textWrap: TextWrap? = DefaultTextWrap.NO_WRAP,
    val lineSpacing: Float = 1F,
    var rotation: Short? = 0, //TODO move it to another attribute
) : Attribute<TextStylesAttribute>() {

    @TabulateMarker
    class Builder(target: Class<out AttributeAware>) : AttributeBuilder<TextStylesAttribute>(target), DefaultColorWords,
        DefaultFontWords, WeightStyleWords, DefaultTextWrapWords {
        override var fontFamily: Font? by observable(null)
        var fontSize: Int? by observable(10)
        override var weight: WeightStyle? by observable(DefaultWeightStyle.NORMAL)
        var italic: Boolean? by observable(false)
        var strikeout: Boolean? by observable(false)
        var underline: Boolean? by observable(false)
        override var color: Color? by observable(null)
        var ident: Short by observable(0)

        @Deprecated("Currently for backward compatibility", replaceWith = ReplaceWith("textWrap"))
        var wrapText: Boolean by observable(false)
        var rotation: Short? by observable(0)
        var lineSpacing: Float by observable(1F)
        override var textWrap: TextWrap by observable(DefaultTextWrap.NO_WRAP)

        override fun provide(): TextStylesAttribute =
            TextStylesAttribute(
                fontFamily,
                fontSize,
                weight,
                italic,
                strikeout,
                underline,
                color,
                ident,
                wrapText,
                textWrap,
                lineSpacing,
                rotation
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
        lineSpacing = takeIfChanged(other, TextStylesAttribute::lineSpacing)
    )

    companion object {
        @JvmStatic
        fun builder(target: Class<out AttributeAware>): Builder =
            Builder(target)

        @JvmStatic
        inline fun <reified AC : AttributeAware> builder(): Builder =
            Builder(AC::class.java)
    }
}
