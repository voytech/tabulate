package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.*
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
    override val leftTopBorderRadius: Width = Width(0F, UnitsOfMeasure.PT),

    override val rightBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val rightBorderColor: Color? = null,
    override val rightBorderWidth: Width = Width(1F, UnitsOfMeasure.PT),
    override val rightTopBorderRadius: Width = Width(0F, UnitsOfMeasure.PT),

    override val topBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val topBorderColor: Color? = null,
    override val topBorderHeight: Height = Height(1F, UnitsOfMeasure.PT),
    override val rightBottomBorderRadius: Width = Width(0F, UnitsOfMeasure.PT),

    override val bottomBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val bottomBorderColor: Color? = null,
    override val bottomBorderHeight: Height = Height(1F, UnitsOfMeasure.PT),
    override val leftBottomBorderRadius: Width = Width(0F, UnitsOfMeasure.PT),
) : Attribute<BordersAttribute>(), Borders {

    @TabulateMarker
    class SingleBorderBuilder : DefaultBorderStyleWords, DefaultColorWords {
        override var style: BorderStyle = DefaultBorderStyle.SOLID
        var measure: Float = 1F
        var uom: UnitsOfMeasure = UnitsOfMeasure.PT
        override var color: Color? = Colors.BLACK
        var radius: Float = 0F

        fun Number.pt() {
            measure = toFloat()
            uom = UnitsOfMeasure.PT
        }

        fun Number.px() {
            measure = toFloat()
            uom = UnitsOfMeasure.PX
        }

        fun Number.radiusPt() {
            radius = toFloat()
        }

        fun Number.radiusPx() {
            radius = toFloat()
        }
    }

    @TabulateMarker
    class Builder(target: Set<Class<out AttributeAware>>) : AttributeBuilder<BordersAttribute>(target) {
        var leftBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var leftBorderColor: Color? by observable(null)
        var leftBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))
        var leftTopBorderRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var rightTopBorderRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var rightBottomBorderRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var leftBottomBorderRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var rightBorderColor: Color? by observable(null)
        var rightBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))
        var topBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var topBorderColor: Color? by observable(null)
        var topBorderHeight: Height by observable(Height(1F, UnitsOfMeasure.PT))
        var rightBottomBorderRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var bottomBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var leftBottomBorderRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var bottomBorderColor: Color? by observable(null)
        var bottomBorderHeight: Height by observable(Height(1F, UnitsOfMeasure.PT))

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
                leftBorderWidth = Width(it.measure, it.uom)
                leftTopBorderRadius = Width(it.radius, it.uom)
            }
        }

        fun right(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                rightBorderColor = it.color
                rightBorderStyle = it.style
                rightBorderWidth = Width(it.measure, it.uom)
                rightTopBorderRadius = Width(it.radius, it.uom)
            }
        }

        fun top(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                topBorderColor = it.color
                topBorderStyle = it.style
                topBorderHeight = Height(it.measure, it.uom)
                rightBottomBorderRadius = Width(it.radius, it.uom)
            }
        }

        fun bottom(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                bottomBorderColor = it.color
                bottomBorderStyle = it.style
                bottomBorderHeight = Height(it.measure, it.uom)
                leftBottomBorderRadius = Width(it.radius, it.uom)
            }
        }

        override fun provide(): BordersAttribute = BordersAttribute(
            leftBorderStyle, leftBorderColor, leftBorderWidth,
            leftTopBorderRadius,
            rightBorderStyle, rightBorderColor, rightBorderWidth, rightTopBorderRadius,
            topBorderStyle, topBorderColor, topBorderHeight,
            rightBottomBorderRadius,
            bottomBorderStyle, bottomBorderColor, bottomBorderHeight, leftBottomBorderRadius
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
        topBorderHeight = takeIfChanged(other, BordersAttribute::topBorderHeight),
        bottomBorderStyle = takeIfChanged(other, BordersAttribute::bottomBorderStyle),
        bottomBorderColor = takeIfChanged(other, BordersAttribute::bottomBorderColor),
        bottomBorderHeight = takeIfChanged(other, BordersAttribute::bottomBorderHeight),
        leftTopBorderRadius = takeIfChanged(other, BordersAttribute::leftTopBorderRadius),
        rightTopBorderRadius = takeIfChanged(other, BordersAttribute::rightTopBorderRadius),
        rightBottomBorderRadius = takeIfChanged(other, BordersAttribute::rightBottomBorderRadius),
        leftBottomBorderRadius = takeIfChanged(other, BordersAttribute::leftBottomBorderRadius),
    )

    companion object {
        @JvmStatic
        fun builder(vararg target: Class<out AttributeAware>): Builder = Builder(setOf(*target))

        @JvmStatic
        inline fun <reified AC : AttributeAware> builder(): Builder = Builder(setOf(AC::class.java))
    }
}

fun BordersAttribute.haveEqualColors(): Boolean =
    (leftBorderColor == rightBorderColor) && (rightBorderColor == topBorderColor) && (topBorderColor == bottomBorderColor)

fun BordersAttribute.haveEqualStyles(): Boolean =
    (leftBorderStyle == rightBorderStyle) && (rightBorderStyle == topBorderStyle) && (topBorderStyle == bottomBorderStyle)

fun BordersAttribute.haveEqualWidths(): Boolean =
    (leftBorderWidth == rightBorderWidth) &&
            (rightBorderWidth.value == topBorderHeight.value) &&
            (rightBorderWidth.unit == topBorderHeight.unit) &&
            (topBorderHeight == bottomBorderHeight)

fun BordersAttribute.areAllEqual(): Boolean = haveEqualStyles() && haveEqualColors() && haveEqualWidths()

fun BordersAttribute.color(): Color? =
    if (haveEqualColors()) leftBorderColor else error("Cannot determine color of all kinds of borders")

fun BordersAttribute.width(): Width =
    if (haveEqualWidths()) leftBorderWidth else error("Cannot determine width of all kinds of borders")

fun BordersAttribute.style(): BorderStyle? =
    if (haveEqualStyles()) leftBorderStyle else error("Cannot determine line style of all kinds of borders")
