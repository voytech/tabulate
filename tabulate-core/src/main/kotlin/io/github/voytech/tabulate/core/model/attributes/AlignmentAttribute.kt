package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.alignment.HorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.VerticalAlignment
import io.github.voytech.tabulate.core.template.operation.AttributedContext

data class AlignmentAttribute(
    val vertical: VerticalAlignment? = DefaultVerticalAlignment.BOTTOM,
    val horizontal: HorizontalAlignment? = DefaultHorizontalAlignment.LEFT
) : Attribute<AlignmentAttribute>() {

    @TabulateMarker
    class Builder(target: Class<out AttributedContext>) : AttributeBuilder<AlignmentAttribute>(target) {
        var vertical: VerticalAlignment? by observable(DefaultVerticalAlignment.BOTTOM)
        var horizontal: HorizontalAlignment? by observable(DefaultHorizontalAlignment.LEFT)
        override fun provide(): AlignmentAttribute = AlignmentAttribute(vertical, horizontal)
    }

    override fun overrideWith(other: AlignmentAttribute): AlignmentAttribute = AlignmentAttribute(
        vertical = takeIfChanged(other, AlignmentAttribute::vertical),
        horizontal = takeIfChanged(other, AlignmentAttribute::horizontal),
    )

    companion object {
        @JvmStatic
        fun  builder(target: Class<out AttributedContext>) : Builder = Builder(target)

        @JvmSynthetic
        @JvmStatic
        inline fun <reified AC: AttributedContext> builder() : Builder = Builder(AC::class.java)
    }

}
