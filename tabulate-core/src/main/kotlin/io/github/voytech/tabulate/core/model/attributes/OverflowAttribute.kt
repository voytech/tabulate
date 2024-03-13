package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.ModelWithAttributes
import io.github.voytech.tabulate.core.model.overflow.Overflow
import io.github.voytech.tabulate.core.model.overflow.OverflowWords

/**
 * This attribute is meant to be compatible only with [ModelWithAttributes] derived classes.
 * It cannot be provided onto [Renderable] because handling of this attribute is sole role of [ModelWithAttributes]
 * specific logic and plays with model rendering iterations concept.
 *
 * @author Wojciech Mąka
 * @since 0.2.0
 */
data class HorizontalOverflowAttribute(val overflow: Overflow) : Attribute<HorizontalOverflowAttribute>() {
    @TabulateMarker
    class Builder(target: Class<out ModelWithAttributes>) : AttributeBuilder<HorizontalOverflowAttribute>(target),
        OverflowWords {
        override var overflow: Overflow by observable(Overflow.RETRY)
        override fun provide(): HorizontalOverflowAttribute = HorizontalOverflowAttribute(overflow)
    }

    override fun overrideWith(other: HorizontalOverflowAttribute): HorizontalOverflowAttribute =
        HorizontalOverflowAttribute(
            overflow = takeIfChanged(other, HorizontalOverflowAttribute::overflow)
        )

    companion object {
        @JvmStatic
        fun builder(target: Class<out ModelWithAttributes>): Builder =
            Builder(target)

        @JvmStatic
        inline fun <reified AC : ModelWithAttributes> builder(): Builder =
            Builder(AC::class.java)
    }
}

data class VerticalOverflowAttribute(val overflow: Overflow) : Attribute<VerticalOverflowAttribute>() {
    @TabulateMarker
    class Builder(target: Class<out ModelWithAttributes>) : AttributeBuilder<VerticalOverflowAttribute>(target),
        OverflowWords {
        override var overflow: Overflow by observable(Overflow.RETRY)
        override fun provide(): VerticalOverflowAttribute = VerticalOverflowAttribute(overflow)
    }

    override fun overrideWith(other: VerticalOverflowAttribute): VerticalOverflowAttribute = VerticalOverflowAttribute(
        overflow = takeIfChanged(other, VerticalOverflowAttribute::overflow)
    )

    companion object {
        @JvmStatic
        fun builder(target: Class<out ModelWithAttributes>): Builder =
            Builder(target)

        @JvmStatic
        inline fun <reified AC : ModelWithAttributes> builder(): Builder =
            Builder(AC::class.java)
    }
}