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
    override val leftBorderStyle: BorderStyle = DefaultBorderStyle.NONE,
    override val leftBorderColor: Color? = null,
    override val leftBorderWidth: Width = Width(1F, UnitsOfMeasure.PT),

    override val rightBorderStyle: BorderStyle = DefaultBorderStyle.NONE,
    override val rightBorderColor: Color? = null,
    override val rightBorderWidth: Width = Width(1F, UnitsOfMeasure.PT),

    override val topBorderStyle: BorderStyle = DefaultBorderStyle.NONE,
    override val topBorderColor: Color? = null,
    override val topBorderHeight: Height = Height(1F, UnitsOfMeasure.PT),

    override val bottomBorderStyle: BorderStyle = DefaultBorderStyle.NONE,
    override val bottomBorderColor: Color? = null,
    override val bottomBorderHeight: Height = Height(1F, UnitsOfMeasure.PT),
    override val leftTopBorderCornerRadius: Width = Width(0F, UnitsOfMeasure.PT),
    override val leftBottomBorderCornerRadius: Width = Width(0F, UnitsOfMeasure.PT),
    override val rightBottomBorderCornerRadius: Width = Width(0F, UnitsOfMeasure.PT),
    override val rightTopBorderCornerRadius: Width = Width(0F, UnitsOfMeasure.PT),

    ) : Attribute<BordersAttribute>(), Borders {

    @TabulateMarker
    class SingleBorderBuilder : DefaultBorderStyleWords, DefaultColorWords {
        override var style: BorderStyle = DefaultBorderStyle.SOLID
        var measure: Float = 1F
        var uom: UnitsOfMeasure = UnitsOfMeasure.PT
        override var color: Color? = Colors.BLACK

        fun Number.pt() {
            measure = toFloat()
            uom = UnitsOfMeasure.PT
        }

        fun Number.px() {
            measure = toFloat()
            uom = UnitsOfMeasure.PX
        }

    }

    @TabulateMarker
    class CornerBorderBuilder : DefaultBorderStyleWords, DefaultColorWords {
        private var uom: UnitsOfMeasure = UnitsOfMeasure.PT
        private var radius: Float = 0F

        fun Number.pt() {
            radius = toFloat()
            uom = UnitsOfMeasure.PT
        }

        fun Number.px() {
            radius = toFloat()
            uom = UnitsOfMeasure.PX
        }

    }


    @TabulateMarker
    class Builder(target: Set<Class<out AttributeAware>>) : AttributeBuilder<BordersAttribute>(target) {
        var leftBorderStyle: BorderStyle by observable(DefaultBorderStyle.NONE)
        var leftBorderColor: Color? by observable(null)
        var leftBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))
        var rightBorderStyle: BorderStyle by observable(DefaultBorderStyle.NONE)
        var rightBorderColor: Color? by observable(null)
        var rightBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))
        var topBorderStyle: BorderStyle by observable(DefaultBorderStyle.NONE)
        var topBorderColor: Color? by observable(null)
        var topBorderHeight: Height by observable(Height(1F, UnitsOfMeasure.PT))
        var bottomBorderStyle: BorderStyle by observable(DefaultBorderStyle.NONE)
        var bottomBorderColor: Color? by observable(null)
        var bottomBorderHeight: Height by observable(Height(1F, UnitsOfMeasure.PT))

        var leftTopBorderStyle: BorderStyle by observable(DefaultBorderStyle.NONE)
        var leftTopBorderColor: Color? by observable(null)
        var leftTopBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))

        var rightTopBorderStyle: BorderStyle by observable(DefaultBorderStyle.NONE)
        var rightTopBorderColor: Color? by observable(null)
        var rightTopBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))

        var leftBottomBorderStyle: BorderStyle by observable(DefaultBorderStyle.NONE)
        var leftBottomBorderColor: Color? by observable(null)
        var leftBottomBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))

        var rightBottomBorderStyle: BorderStyle by observable(DefaultBorderStyle.NONE)
        var rightBottomBorderColor: Color? by observable(null)
        var rightBottomBorderWidth: Width by observable(Width(1F, UnitsOfMeasure.PT))

        var leftTopBorderCornerRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var rightTopBorderCornerRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var leftBottomBorderCornerRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))
        var rightBottomBorderCornerRadius: Width by observable(Width(0F, UnitsOfMeasure.PT))

        fun all(block: SingleBorderBuilder.() -> Unit) {
            top(block)
            left(block)
            right(block)
            bottom(block)
        }

        fun corners(block: CornerBorderBuilder.() -> Unit) {
            leftTopCorner(block)
            leftBottomCorner(block)
            rightTopCorner(block)
            rightBottomCorner(block)
        }

        fun leftTopCorner(block: CornerBorderBuilder.() -> Unit) {
            CornerBorderBuilder().apply(block).let {
                leftTopBorderCornerRadius = Width(it.radius, it.uom)
                rightTopBorderCornerRadius = Width(it.radius, it.uom)
                rightBottomBorderCornerRadius = Width(it.radius, it.uom)
                leftBottomBorderCornerRadius = Width(it.radius, it.uom)
            }
        }

        fun left(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                leftBorderColor = it.color
                leftBorderStyle = it.style
                leftBorderWidth = Width(it.measure, it.uom)
            }
        }

        fun rightTopCorner(block: CornerBorderBuilder.() -> Unit) {
            CornerBorderBuilder().apply(block).let {
            }
        }

        fun right(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                rightBorderColor = it.color
                rightBorderStyle = it.style
                rightBorderWidth = Width(it.measure, it.uom)
            }
        }

        fun rightBottomCorner(block: CornerBorderBuilder.() -> Unit) {
            CornerBorderBuilder().apply(block).let {
            }
        }

        fun top(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                topBorderColor = it.color
                topBorderStyle = it.style
                topBorderHeight = Height(it.measure, it.uom)
            }
        }

        fun leftBottomCorner(block: CornerBorderBuilder.() -> Unit) {
            CornerBorderBuilder().apply(block).let {
            }
        }


        fun bottom(block: SingleBorderBuilder.() -> Unit) {
            SingleBorderBuilder().apply(block).let {
                bottomBorderColor = it.color
                bottomBorderStyle = it.style
                bottomBorderHeight = Height(it.measure, it.uom)
            }
        }

        override fun provide(): BordersAttribute = BordersAttribute(
            leftBorderStyle, leftBorderColor, leftBorderWidth,
            rightBorderStyle, rightBorderColor, rightBorderWidth,
            topBorderStyle, topBorderColor, topBorderHeight,
            bottomBorderStyle, bottomBorderColor, bottomBorderHeight,
            leftTopBorderCornerRadius, leftTopBorderStyle, leftTopBorderColor, leftTopBorderWidth,
            rightTopBorderCornerRadius, rightTopBorderStyle, rightTopBorderColor, rightTopBorderWidth,
            leftBottomBorderCornerRadius, leftBottomBorderStyle, leftBottomBorderColor, leftBottomBorderWidth,
            rightBottomBorderCornerRadius, rightBottomBorderStyle, rightBottomBorderColor, rightBottomBorderWidth,
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
        leftTopBorderCornerRadius = takeIfChanged(other, BordersAttribute::leftTopBorderCornerRadius),
        leftTopBorderCornerStyle = takeIfChanged(other, BordersAttribute::leftTopBorderCornerStyle),
        leftTopBorderCornerColor = takeIfChanged(other, BordersAttribute::leftTopBorderCornerColor),
        leftTopBorderCornerWidth = takeIfChanged(other, BordersAttribute::leftTopBorderCornerWidth),
        rightTopBorderCornerRadius = takeIfChanged(other, BordersAttribute::rightTopBorderCornerRadius),
        rightTopBorderCornerStyle = takeIfChanged(other, BordersAttribute::rightTopBorderCornerStyle),
        rightTopBorderCornerColor = takeIfChanged(other, BordersAttribute::rightTopBorderCornerColor),
        rightTopBorderCornerWidth = takeIfChanged(other, BordersAttribute::rightTopBorderCornerWidth),
        rightBottomBorderCornerRadius = takeIfChanged(other, BordersAttribute::rightBottomBorderCornerRadius),
        rightBottomBorderCornerStyle = takeIfChanged(other, BordersAttribute::rightBottomBorderCornerStyle),
        rightBottomBorderCornerColor = takeIfChanged(other, BordersAttribute::rightBottomBorderCornerColor),
        rightBottomBorderCornerWidth = takeIfChanged(other, BordersAttribute::rightBottomBorderCornerWidth),
        leftBottomBorderCornerRadius = takeIfChanged(other, BordersAttribute::leftBottomBorderCornerRadius),
        leftBottomBorderCornerStyle = takeIfChanged(other, BordersAttribute::leftBottomBorderCornerStyle),
        leftBottomBorderCornerWidth = takeIfChanged(other, BordersAttribute::leftBottomBorderCornerWidth),
        leftBottomBorderCornerColor = takeIfChanged(other, BordersAttribute::leftBottomBorderCornerColor),
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

fun BorderStyle?.isSingleLine(): Boolean =
    this == DefaultBorderStyle.SOLID || this == DefaultBorderStyle.DOTTED || this == DefaultBorderStyle.DASHED

fun Borders.hasOnlySingleLineStyle(): Boolean =
    leftBorderStyle.isSingleLine() &&
    rightBorderStyle.isSingleLine() &&
    topBorderStyle.isSingleLine() &&
    bottomBorderStyle.isSingleLine() &&
    rightBorderStyle.isSingleLine() &&
    leftTopBorderCornerStyle.isSingleLine() &&
    rightTopBorderCornerStyle.isSingleLine() &&
    leftBottomBorderCornerStyle.isSingleLine() &&
    rightBottomBorderCornerStyle.isSingleLine()


fun BordersAttribute.areAllEqual(): Boolean = haveEqualStyles() && haveEqualColors() && haveEqualWidths()

fun BordersAttribute.color(): Color? =
    if (haveEqualColors()) leftBorderColor else error("Cannot determine color of all kinds of borders")

fun BordersAttribute.width(): Width =
    if (haveEqualWidths()) leftBorderWidth else error("Cannot determine width of all kinds of borders")

fun BordersAttribute.style(): BorderStyle? =
    if (haveEqualStyles()) leftBorderStyle else error("Cannot determine line style of all kinds of borders")
