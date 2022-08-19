package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.MeasuredValue
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.AttributedContext

data class WidthAttribute(
    val auto: Boolean = false,
    val value: Width = Width.zero(UnitsOfMeasure.PX),
) : Attribute<WidthAttribute>(), LayoutElement {

    @TabulateMarker
    class Builder(target: Class<out AttributedContext>) : AttributeBuilder<WidthAttribute>(target) {
        var auto: Boolean by observable(false)
        var value: Width by observable(Width.zero(UnitsOfMeasure.PX))

        fun Number.pt()  {
            value = MeasuredValue(toFloat(), UnitsOfMeasure.PT).width()
        }
        fun Number.px() {
            value = MeasuredValue(toFloat(), UnitsOfMeasure.PX).width()
        }

        override fun provide(): WidthAttribute = WidthAttribute(auto, value)
    }

    /**
     * When overriding 'width' attribute:
     * 1) if only 'px' property has changed and its value is greater than 0 that means one want to
     * disable 'auto' property.
     * 2) if only 'auto' property has changed - take its new value. Regardless of 'px' property value 'auto' property
     * should force automatic width resolution.
     */
    override fun overrideWith(other: WidthAttribute): WidthAttribute =
        takeIfChanged(other, WidthAttribute::value).let { newWidth ->
            WidthAttribute(
                value = newWidth,
                auto = takeIfChanged(other, WidthAttribute::auto).let { _auto ->
                    if (newWidth != value && newWidth.value > 0 && _auto == auto) false else _auto
                }
            )
        }

    override fun Layout<*,*,*>.computeBoundaries(): LayoutElementBoundingBox =
        if (!auto) query.elementBoundaries(width = value)
        else query.elementBoundaries()

    companion object {
        @JvmStatic
        fun  builder(target: Class<out AttributedContext>) : Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC: AttributedContext> builder() : Builder = Builder(AC::class.java)
    }

}