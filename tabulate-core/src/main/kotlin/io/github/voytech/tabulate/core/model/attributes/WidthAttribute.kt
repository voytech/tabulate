package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width

data class WidthAttribute(
    val value: Width = Width.zero(UnitsOfMeasure.PX),
) : Attribute<WidthAttribute>() {

    @TabulateMarker
    class Builder(vararg target: Class<out AttributeAware>) : AttributeBuilder<WidthAttribute>(setOf(*target)) {
        var value: Width by observable(Width.zero(UnitsOfMeasure.PX))

        fun Number.pt()  {
            value = Width(toFloat(), UnitsOfMeasure.PT)
        }
        fun Number.px() {
            value = Width(toFloat(), UnitsOfMeasure.PX)
        }

        fun Number.percents() {
            value = Width(toFloat(), UnitsOfMeasure.PC)
        }

        override fun provide(): WidthAttribute = WidthAttribute(value)
    }

    /**
     * When overriding 'width' attribute:
     * 1) if only 'px' property has changed and its value is greater than 0 that means one want to
     * disable 'auto' property.
     * 2) if only 'auto' property has changed - take its new value. Regardless of 'px' property value 'auto' property
     * should force automatic width resolution.
     */
    override fun overrideWith(other: WidthAttribute): WidthAttribute =
        WidthAttribute(value = takeIfChanged(other, WidthAttribute::value))

    companion object {
        @JvmStatic
        fun  builder(target: Class<out AttributeAware>) : Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC: AttributeAware> builder() : Builder = Builder(AC::class.java)
    }

}