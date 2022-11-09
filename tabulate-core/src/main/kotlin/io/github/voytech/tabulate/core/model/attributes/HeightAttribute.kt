package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.MeasuredValue
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.template.layout.*
import io.github.voytech.tabulate.core.template.operation.AttributedContext

data class HeightAttribute(
    val value: Height = Height.zero(UnitsOfMeasure.PX)
) : Attribute<HeightAttribute>(),
    BoundingBoxModifier {

    override fun overrideWith(other: HeightAttribute): HeightAttribute = HeightAttribute(
        value = takeIfChanged(other, HeightAttribute::value)
    )

    @TabulateMarker
    class Builder(target: Class<out AttributedContext>) : AttributeBuilder<HeightAttribute>(target) {
        var value: Height by observable(Height.zero(UnitsOfMeasure.PX))

        fun Number.pt()  {
            value = MeasuredValue(toFloat(), UnitsOfMeasure.PT).height()
        }
        fun Number.px() {
            value = MeasuredValue(toFloat(), UnitsOfMeasure.PX).height()
        }

        override fun provide(): HeightAttribute = HeightAttribute(value)
    }

    override fun Layout<*, *, *>.alter(source: LayoutElementBoundingBox): LayoutElementBoundingBox =
        source.apply { height = value }

    companion object {
        @JvmStatic
        fun  builder(target: Class<out AttributedContext>) : Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC: AttributedContext> builder() : Builder = Builder(AC::class.java)
    }

}
