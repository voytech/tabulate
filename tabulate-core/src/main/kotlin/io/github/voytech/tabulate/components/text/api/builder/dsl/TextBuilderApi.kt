package io.github.voytech.tabulate.components.text.api.builder.dsl

import io.github.voytech.tabulate.components.container.api.builder.dsl.ContainerBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.FooterBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.HeaderBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.text.api.builder.TextBuilderState
import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.WrapperBuilderApi
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.ModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.ExecutionContext
import io.github.voytech.tabulate.core.model.ReifiedValueSupplier


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
    inline fun <reified C : ExecutionContext> value(noinline supplier: (C) -> String) {
        value(C::class.java, supplier)
    }

    @JvmSynthetic
    fun <C : ExecutionContext> value(clazz: Class<C>, supplier: (C) -> String) {
        builder.valueSupplier = ReifiedValueSupplier(clazz, String::class.java, supplier)
    }

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

//TODO introduce api marker on which API builders below can be installed in one shot
fun HeaderBuilderApi.text(block: TextBuilderApi.() -> Unit) = bind(TextBuilderApi().apply(block))

fun FooterBuilderApi.text(block: TextBuilderApi.() -> Unit) = bind(TextBuilderApi().apply(block))

fun PageBuilderApi.text(block: TextBuilderApi.() -> Unit) = bind(TextBuilderApi().apply(block))

fun ContainerBuilderApi.text(block: TextBuilderApi.() -> Unit) = bind(TextBuilderApi().apply(block))

infix fun WrapperBuilderApi.text(block: TextBuilderApi.() -> Unit) = bind(TextBuilderApi().apply(block))
