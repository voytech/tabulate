package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.*
import kotlin.properties.Delegates

data class MarginsAttribute(
    val left: X = X.zero(UnitsOfMeasure.PT),
    val top: Y = Y.zero(UnitsOfMeasure.PT),
    val right: X = X.zero(UnitsOfMeasure.PT),
    val bottom: Y = Y.zero(UnitsOfMeasure.PT),
) : Attribute<MarginsAttribute>() {

    @TabulateMarker
    class Builder(target: Class<out AttributeAware>) : AttributeBuilder<MarginsAttribute>(target) {
        var left: X by observable(X.zero(UnitsOfMeasure.PT))
        var top: Y by observable(Y.zero(UnitsOfMeasure.PT))
        var right: X by observable(X.zero(UnitsOfMeasure.PT))
        var bottom: Y by observable(Y.zero(UnitsOfMeasure.PT))


        inline fun <reified T : Measure<T>> Number.pt(): T = T::class.java.new(toFloat(), UnitsOfMeasure.PT)

        inline fun <reified T : Measure<T>> Number.px(): T = T::class.java.new(toFloat(), UnitsOfMeasure.PX)

        inline fun <reified T : Measure<T>> Number.percents(): T = T::class.java.new(toFloat(), UnitsOfMeasure.PC)

        fun all(block: () -> X) {
            block().let {
                left { it }
                top { Y(it.value, it.unit) }
                right { it }
                bottom { Y(it.value, it.unit) }
            }
        }

        fun left(block: () -> X) {
            left = block()
        }

        fun top(block: () -> Y) {
            top = block()
        }

        fun right(block: () -> X) {
            right = block()
        }

        fun bottom(block: () -> Y) {
            bottom = block()
        }

        override fun provide(): MarginsAttribute = MarginsAttribute(left, top, right, bottom)
    }

    companion object {
        @JvmStatic
        fun builder(target: Class<out AttributeAware>): Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC : AttributeAware> builder(): Builder = Builder(AC::class.java)
    }

}