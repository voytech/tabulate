package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure

data class HeightAttribute(
    val value: Height = Height.zero(UnitsOfMeasure.PX)
) : Attribute<HeightAttribute>() {

    override fun overrideWith(other: HeightAttribute): HeightAttribute = HeightAttribute(
        value = takeIfChanged(other, HeightAttribute::value)
    )

    @TabulateMarker
    class Builder(vararg target: Class<out AttributeAware>) : AttributeBuilder<HeightAttribute>(setOf(*target)) {
        var value: Height by observable(Height.zero(UnitsOfMeasure.PX))

        fun Number.pt()  {
            value = Height(toFloat(), UnitsOfMeasure.PT)
        }
        fun Number.px() {
            value = Height(toFloat(), UnitsOfMeasure.PX)
        }

        fun Number.percents() {
            value = Height(toFloat(), UnitsOfMeasure.PC)
        }

        override fun provide(): HeightAttribute = HeightAttribute(value)
    }

    companion object {
        @JvmStatic
        fun  builder(target: Class<out AttributeAware>) : Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC: AttributeAware> builder() : Builder = Builder(AC::class.java)
    }

}
