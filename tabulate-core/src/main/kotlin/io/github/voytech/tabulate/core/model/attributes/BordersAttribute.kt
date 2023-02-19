package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.MeasuredValue
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.border.BorderStyle
import io.github.voytech.tabulate.core.model.border.Borders
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.template.operation.AttributedContext

data class BordersAttribute(
    override val leftBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val leftBorderColor: Color? = null,
    override val leftBorderWidth: Width = Width(1F,UnitsOfMeasure.PT),

    override val rightBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val rightBorderColor: Color? = null,
    override val rightBorderWidth: Width = Width(1F,UnitsOfMeasure.PT),

    override val topBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val topBorderColor: Color? = null,
    override val topBorderWidth: Width = Width(1F,UnitsOfMeasure.PT),

    override val bottomBorderStyle: BorderStyle? = DefaultBorderStyle.NONE,
    override val bottomBorderColor: Color? = null,
    override val bottomBorderWidth: Width = Width(1F,UnitsOfMeasure.PT),
) : Attribute<BordersAttribute>(), Borders {

    @TabulateMarker
    class AllBordersBuilder {
        var style: BorderStyle = DefaultBorderStyle.SOLID
        var width: Width = Width(1F,UnitsOfMeasure.PT)
        var color: Color = Colors.BLACK
        fun Number.pt(): Width = MeasuredValue(toFloat(), UnitsOfMeasure.PT).width()
        fun Number.px(): Width = MeasuredValue(toFloat(), UnitsOfMeasure.PX).width()
    }

    @TabulateMarker
    class Builder(target: Class<out AttributedContext>) : AttributeBuilder<BordersAttribute>(target) {
        var leftBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var leftBorderColor: Color? by observable(null)
        var leftBorderWidth: Width by observable(Width(1F,UnitsOfMeasure.PT))
        var rightBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var rightBorderColor: Color? by observable(null)
        var rightBorderWidth: Width by observable(Width(1F,UnitsOfMeasure.PT))
        var topBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var topBorderColor: Color? by observable(null)
        var topBorderWidth: Width by observable(Width(1F,UnitsOfMeasure.PT))
        var bottomBorderStyle: BorderStyle? by observable(DefaultBorderStyle.NONE)
        var bottomBorderColor: Color? by observable(null)
        var bottomBorderWidth: Width by observable(Width(1F,UnitsOfMeasure.PT))

        fun Number.pt(): Width = MeasuredValue(toFloat(), UnitsOfMeasure.PT).width()
        fun Number.px(): Width = MeasuredValue(toFloat(), UnitsOfMeasure.PX).width()

        fun all(block: AllBordersBuilder.() -> Unit) {
            AllBordersBuilder().apply(block).let {
                leftBorderColor = it.color
                rightBorderColor = it.color
                topBorderColor = it.color
                bottomBorderColor = it.color
                leftBorderStyle = it.style
                rightBorderStyle = it.style
                topBorderStyle = it.style
                bottomBorderStyle = it.style
                leftBorderWidth = it.width
                rightBorderWidth = it.width
                topBorderWidth = it.width
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
        fun  builder(target: Class<out AttributedContext>) : Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC: AttributedContext> builder() : Builder = Builder(AC::class.java)
    }
}

