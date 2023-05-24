package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.border.BorderStyle
import io.github.voytech.tabulate.core.model.border.Borders
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyleWords
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.color.DefaultColorWords

data class BordersAttribute(
    override val leftBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val leftBorderColor: Color? = null,
    override val leftBorderWidth: Width = Width(1F, UnitsOfMeasure.PT),

    override val rightBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val rightBorderColor: Color? = null,
    override val rightBorderWidth: Width = Width(1F, UnitsOfMeasure.PT),

    override val topBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val topBorderColor: Color? = null,
    override val topBorderWidth: Width = Width(1F, UnitsOfMeasure.PT),

    override val bottomBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val bottomBorderColor: Color? = null,
    override val bottomBorderWidth: Width = Width(1F, UnitsOfMeasure.PT),
) : Attribute<BordersAttribute>(), Borders {

    @TabulateMarker
    class SingleBorderBuilder : DefaultBorderStyleWords, DefaultColorWords {
        override var style: BorderStyle = DefaultBorderStyle.SOLID
        var width: Width = Width(1F, UnitsOfMeasure.PT)
        override var color: Color? = Colors.BLACK

        fun Number.pt()  {
            width = Width(toFloat(), UnitsOfMeasure.PT)
        }
        fun Number.px() {
           width = Width(toFloat(), UnitsOfMeasure.PX)
        }
    }

    @TabulateMarker
    class Builder(target: Class<out AttributeAware>) : AttributeBuilder<BordersAttribute>(target) {
        var leftBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var leftBorderColor: Color? by observable(null)
        var leftBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))
        var rightBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var rightBorderColor: Color? by observable(null)
        var rightBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))
        var topBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var topBorderColor: Color? by observable(null)
        var topBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))
        var bottomBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var bottomBorderColor: Color? by observable(null)
        var bottomBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))

        fun Number.pt(): Width = Width(toFloat(), UnitsOfMeasure.PT)
        fun Number.px(): Width = Width(toFloat(), UnitsOfMeasure.PX)

        fun all(block: SingleBorderBuilder.() -> Unit) {
            top(block)
            left(block)
            right(block)
            bottom(block)
        }

        fun left(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                leftBorderColor = it.color
                leftBorderStyle = it.style
                leftBorderWidth = it.width
            }
        }

        fun right(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                rightBorderColor = it.color
                rightBorderStyle = it.style
                rightBorderWidth = it.width
            }
        }

        fun top(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                topBorderColor = it.color
                topBorderStyle = it.style
                topBorderWidth = it.width
            }
        }

        fun bottom(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                bottomBorderColor = it.color
                bottomBorderStyle = it.style
                bottomBorderWidth = it.width
            }
        }

        override fun provide(): BordersAttribute = BordersAttribute(
            leftBorderStyle, leftBorderColor, leftBorderWidth,
            rightBorderStyle, rightBorderColor, rightBorderWidth,
            topBorderStyle, topBorderColor, topBorderWidth,
            bottomBorderStyle, bottomBorderColor, bottomBorderWidth
        )
    }

    override fun overrideWith(other: BordersAttribute): BordersAttribute = BordersAttribute(
        leftBorderStyle = takeIfChanged(other, BordersAttribute::leftBorderStyle),
        leftBorderColor = takeIfChanged(other, BordersAttribute::leftBorderColor),
        leftBorderWidth = takeIfChanged(other, BordersAttribute::leftBorderWidth),
        rightBorderStyle = takeIfChanged(other, BordersAttribute::rightBorderStyle),
        rightBorderColor = takeIfChanged(other, BordersAttribute::rightBorderColor),
        rightBorderWidth = takeIfChanged(other, BordersAttribute::rightBorderWidth),
        topBorderStyle = takeIfChanged(other, BordersAttribute::topBorderStyle),
        topBorderColor = takeIfChanged(other, BordersAttribute::topBorderColor),
        topBorderWidth = takeIfChanged(other, BordersAttribute::topBorderWidth),
        bottomBorderStyle = takeIfChanged(other, BordersAttribute::bottomBorderStyle),
        bottomBorderColor = takeIfChanged(other, BordersAttribute::bottomBorderColor),
        bottomBorderWidth = takeIfChanged(other, BordersAttribute::bottomBorderWidth),
    )

    companion object {
        @JvmStatic
        fun builder(target: Class<out AttributeAware>): Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC : AttributeAware> builder(): Builder = Builder(AC::class.java)
    }
}

fun BordersAttribute.haveEqualColors(): Boolean =
    (leftBorderColor == rightBorderColor) && (rightBorderColor == topBorderColor) && (topBorderColor == bottomBorderColor)

fun BordersAttribute.haveEqualStyles(): Boolean =
    (leftBorderStyle == rightBorderStyle) && (rightBorderStyle == topBorderStyle) && (topBorderStyle == bottomBorderStyle)

fun BordersAttribute.haveEqualWidths(): Boolean =
    (leftBorderWidth == rightBorderWidth) && (rightBorderWidth == topBorderWidth) && (topBorderWidth == bottomBorderWidth)

fun BordersAttribute.areAllEqual(): Boolean = haveEqualStyles() && haveEqualColors() && haveEqualWidths()

fun BordersAttribute.color(): Color? = if (haveEqualColors()) leftBorderColor else error("Cannot determine color of all kinds of borders")
fun BordersAttribute.width(): Width = if (haveEqualWidths()) leftBorderWidth else error("Cannot determine width of all kinds of borders")
fun BordersAttribute.style(): BorderStyle? = if (haveEqualStyles()) leftBorderStyle else error("Cannot determine line style of all kinds of borders")