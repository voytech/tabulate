package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeAware
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.alignment.HorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.VerticalAlignment

data class AlignmentAttribute(
    val vertical: VerticalAlignment? = DefaultVerticalAlignment.BOTTOM,
    val horizontal: HorizontalAlignment? = DefaultHorizontalAlignment.LEFT
) : Attribute<AlignmentAttribute>() {

    @TabulateMarker
    class Builder(target: Class<out AttributeAware>) : AttributeBuilder<AlignmentAttribute>(target) {
        var vertical: VerticalAlignment? by observable(DefaultVerticalAlignment.BOTTOM)
        var horizontal: HorizontalAlignment? by observable(DefaultHorizontalAlignment.LEFT)

        val center: DSLCommand
            get() {
                horizontal = DefaultHorizontalAlignment.CENTER; return DSLCommand
            }

        val justify: DSLCommand
            get() {
                horizontal = DefaultHorizontalAlignment.JUSTIFY; return DSLCommand
            }

        val left: DSLCommand
            get() {
                horizontal = DefaultHorizontalAlignment.LEFT; return DSLCommand
            }
        val right: DSLCommand
            get() {
                horizontal = DefaultHorizontalAlignment.RIGHT; return DSLCommand
            }
        val middle: DSLCommand
            get() {
                vertical = DefaultVerticalAlignment.MIDDLE; return DSLCommand
            }
        val top: DSLCommand
            get() {
                vertical = DefaultVerticalAlignment.TOP; return DSLCommand
            }
        val bottom: DSLCommand
            get() {
                vertical = DefaultVerticalAlignment.BOTTOM; return DSLCommand
            }

        override fun provide(): AlignmentAttribute = AlignmentAttribute(vertical, horizontal)
    }

    override fun overrideWith(other: AlignmentAttribute): AlignmentAttribute = AlignmentAttribute(
        vertical = takeIfChanged(other, AlignmentAttribute::vertical),
        horizontal = takeIfChanged(other, AlignmentAttribute::horizontal),
    )

    companion object {
        @JvmStatic
        fun builder(target: Class<out AttributeAware>): Builder = Builder(target)

        @JvmSynthetic
        @JvmStatic
        inline fun <reified AC : AttributeAware> builder(): Builder = Builder(AC::class.java)
    }

}
