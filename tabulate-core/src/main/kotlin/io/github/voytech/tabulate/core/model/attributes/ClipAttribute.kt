package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.clip.ClippingMode
import io.github.voytech.tabulate.core.model.clip.DefaultClippingMode
import io.github.voytech.tabulate.core.model.clip.TextClippingModeWords
import io.github.voytech.tabulate.core.operation.Renderable

data class ClipAttribute(
    val mode: ClippingMode = DefaultClippingMode.NO_CLIP,
) : Attribute<ClipAttribute>() {

    @TabulateMarker
    class Builder(target: Class<out AttributeAware>) : AttributeBuilder<ClipAttribute>(target),
        TextClippingModeWords {
        override var mode: ClippingMode by observable(DefaultClippingMode.NO_CLIP)
        override fun provide(): ClipAttribute = ClipAttribute(mode)
    }

    override fun overrideWith(other: ClipAttribute): ClipAttribute = ClipAttribute(
        mode = takeIfChanged(other, ClipAttribute::mode)
    )

    companion object {
        @JvmStatic
        fun  builder(target: Class<out Renderable<*>>) : Builder =
            Builder(target)

        @JvmStatic
        inline fun <reified AC: Renderable<*>> builder() : Builder =
            Builder(AC::class.java)
    }
}
