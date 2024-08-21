package io.github.voytech.tabulate.components.image.api.builder.dsl

import io.github.voytech.tabulate.components.container.api.builder.dsl.ContainerBuilderApi
import io.github.voytech.tabulate.components.image.api.builder.ImageBuilderState
import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.page.api.builder.dsl.FooterBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.HeaderBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.text.api.builder.dsl.TextBuilderApi
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.WrapperBuilderApi
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.ModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import java.util.*


/**
 * Kotlin type-safe DSL table builder API for defining text model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class ImageBuilderApi internal constructor() : ModelBuilderApi<Image, ImageBuilderState>(ImageBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var id: String by this.builder::id

    @set:JvmSynthetic
    @get:JvmSynthetic
    var filePath: String by this.builder::filePath

    @JvmSynthetic
    fun attributes(block: ImageAttributesBuilderApi.() -> Unit) {
        ImageAttributesBuilderApi(builder).apply(block)
    }
}

/**
 * Kotlin type-safe DSL table attribute builder API for defining document level attributes.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.3.0
 */
@TabulateMarker
class ImageAttributesBuilderApi internal constructor(private val builderState: ImageBuilderState) {
    @JvmSynthetic
    fun attribute(attribute: AttributeBuilder<*>) {
        builderState.attribute(attribute)
    }
}

//TODO introduce api marker on which API builders below can be installed in one shot

fun HeaderBuilderApi.image(block: ImageBuilderApi.() -> Unit) = bind(ImageBuilderApi().apply(block))

fun FooterBuilderApi.image(block: ImageBuilderApi.() -> Unit) = bind(ImageBuilderApi().apply(block))

fun PageBuilderApi.image(block: ImageBuilderApi.() -> Unit) = bind(ImageBuilderApi().apply(block))

fun ContainerBuilderApi.image(block: ImageBuilderApi.() -> Unit) = bind(ImageBuilderApi().apply(block))

infix fun WrapperBuilderApi.image(block: TextBuilderApi.() -> Unit) = bind(TextBuilderApi().apply(block))
