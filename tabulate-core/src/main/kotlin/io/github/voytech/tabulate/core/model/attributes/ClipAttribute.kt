package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.clip.ClippingMode
import io.github.voytech.tabulate.core.model.clip.TextClippingModeWords
import io.github.voytech.tabulate.core.operation.RenderableEntity

/**
 * Represents a clipping attribute that defines the clipping mode for rendering components [RenderableEntity].
 * The clipping mode determines how a [RenderableEntity] should be rendered.
 * It can be CLIPPED when rendered to match [RenderableBoundingBox] constraints, or it can be SKIPPED meaning no rendering
 * will be performed. After rendering, rendering operation must return [RenderingResult] containing information if
 * [RenderableEntity] was successfully rendered, clipped or skipped.
 *
 * By default, this setting is set to {@link ClippingMode#SKIP}.
 *
 * @param mode The clipping mode for rendering components. Defaults to {@link ClippingMode#SKIP}.
 * @see ClippingMode
 * @see Attribute
 * @see AttributeBuilder
 * @see TextClippingModeWords
 *
 * @author Wojciech Mąka
 * @since 0.2.0
 */
data class ClipAttribute(
    val mode: ClippingMode = ClippingMode.SKIP,
) : Attribute<ClipAttribute>() {

    @TabulateMarker
    class Builder(target: Class<out AttributeAware>) : AttributeBuilder<ClipAttribute>(target),
        TextClippingModeWords {
        override var mode: ClippingMode by observable(ClippingMode.SKIP)
        override fun provide(): ClipAttribute = ClipAttribute(mode)
    }

    override fun overrideWith(other: ClipAttribute): ClipAttribute = ClipAttribute(
        mode = takeIfChanged(other, ClipAttribute::mode)
    )

    companion object {
        @JvmStatic
        fun  builder(target: Class<out RenderableEntity<*>>) : Builder =
            Builder(target)

        @JvmStatic
        inline fun <reified AC: RenderableEntity<*>> builder() : Builder =
            Builder(AC::class.java)
    }
}
