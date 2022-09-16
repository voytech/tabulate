package io.github.voytech.tabulate.components.text.api.builder.dsl

import io.github.voytech.tabulate.components.page.api.builder.dsl.FooterBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.HeaderBuilderApi
import io.github.voytech.tabulate.components.text.api.builder.TextBuilderState
import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.ModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker


/**
 * Kotlin type-safe DSL table builder API for defining text model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class TextBuilderApi internal constructor() : ModelBuilderApi<Text, TextBuilderState>(TextBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var value: String by this.builder::value

    @JvmSynthetic
    fun attributes(block: TextAttributesBuilderApi.() -> Unit) {
        TextAttributesBuilderApi(builder).apply(block)
    }
}

/**
 * Kotlin type-safe DSL table attribute builder API for defining document level attributes.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.3.0
 */
@TabulateMarker
class TextAttributesBuilderApi internal constructor(private val builderState: TextBuilderState) {
    @JvmSynthetic
    fun attribute(attribute: AttributeBuilder<*>) {
        builderState.attribute(attribute)
    }
}

fun HeaderBuilderApi.text(block: TextBuilderApi.() -> Unit) = bind(TextBuilderApi().apply(block))

fun FooterBuilderApi.text(block: TextBuilderApi.() -> Unit) = bind(TextBuilderApi().apply(block))
