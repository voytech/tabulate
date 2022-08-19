package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElement
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundingBox
import io.github.voytech.tabulate.core.template.layout.elementBoundaries
import io.github.voytech.tabulate.core.template.operation.AttributedContext

data class MarginsAttribute(
    val left: X = X.zero(UnitsOfMeasure.PT),
    val top: Y = Y.zero(UnitsOfMeasure.PT),
) : Attribute<MarginsAttribute>(), LayoutElement {

    @TabulateMarker
    class Builder(target: Class<out AttributedContext>) : AttributeBuilder<MarginsAttribute>(target) {
        var left: X by observable(X.zero(UnitsOfMeasure.PT))
        var top: Y by observable(Y.zero(UnitsOfMeasure.PT))

        fun Number.pt(): MeasuredValue = MeasuredValue(toFloat(), UnitsOfMeasure.PT)
        fun Number.px(): MeasuredValue = MeasuredValue(toFloat(), UnitsOfMeasure.PX)

        fun left(block: () -> MeasuredValue) {
            left = block().x()
        }

        fun top(block: () -> MeasuredValue) {
            top = block().y()
        }

        override fun provide(): MarginsAttribute = MarginsAttribute(left, top)
    }

    override fun Layout<*, *, *>.computeBoundaries(): LayoutElementBoundingBox =
        query.elementBoundaries(
            x = query.getLayoutBoundary().leftTop.x - left,
            y = query.getLayoutBoundary().leftTop.y - top,
            width = Width(left.value, left.unit),
            height = Height(top.value, top.unit)
        )

    companion object {
        @JvmStatic
        fun  builder(target: Class<out AttributedContext>) : Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC: AttributedContext> builder() : Builder =
            Builder(AC::class.java)
    }

}