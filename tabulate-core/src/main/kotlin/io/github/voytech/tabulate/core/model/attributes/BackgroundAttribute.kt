package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.background.DefaultFillStyleWords
import io.github.voytech.tabulate.core.model.background.DefaultFillType
import io.github.voytech.tabulate.core.model.background.FillType
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.color.DefaultColorWords

data class BackgroundAttribute(
    val color: Color? = null,
    val fill: FillType = DefaultFillType.SOLID
) : Attribute<BackgroundAttribute>() {

    @TabulateMarker
    class Builder(target: Class<out AttributeAware>) : AttributeBuilder<BackgroundAttribute>(target),
        DefaultColorWords, DefaultFillStyleWords {
        override var color: Color? by observable(null)
        override var fill: FillType by observable(DefaultFillType.SOLID)

        override fun provide(): BackgroundAttribute = BackgroundAttribute(color, fill)
    }

    override fun overrideWith(other: BackgroundAttribute): BackgroundAttribute = BackgroundAttribute(
        color = takeIfChanged(other, BackgroundAttribute::color),
        fill = takeIfChanged(other, BackgroundAttribute::fill),
    )

    companion object {
        @JvmStatic
        fun builder(target: Class<out AttributeAware>): Builder = Builder(target)

        @JvmStatic
        inline fun <reified AC : AttributeAware> builder(): Builder = Builder(AC::class.java)
    }
}