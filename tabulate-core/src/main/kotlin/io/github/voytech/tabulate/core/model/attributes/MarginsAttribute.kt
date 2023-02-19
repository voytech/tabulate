package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.operation.AttributedContext

data class MarginsAttribute(
    val left: X = X.zero(UnitsOfMeasure.PT),
    val top: Y = Y.zero(UnitsOfMeasure.PT),
) : Attribute<MarginsAttribute>() {

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

    companion object {
        @JvmStatic
        fun  builder(target: Class<out AttributedContext>) : Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC: AttributedContext> builder() : Builder =
            Builder(AC::class.java)
    }

}