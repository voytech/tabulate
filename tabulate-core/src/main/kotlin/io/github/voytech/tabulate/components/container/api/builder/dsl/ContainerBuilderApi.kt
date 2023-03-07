package io.github.voytech.tabulate.components.container.api.builder.dsl

import io.github.voytech.tabulate.components.container.api.builder.ContainerBuilderState
import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Orientation


/**
 * Kotlin type-safe DSL document sheet builder API for defining entire document sheet.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class ContainerBuilderApi internal constructor(): CompositeModelBuilderApi<Container, ContainerBuilderState>(ContainerBuilderState()) {
    @set:JvmSynthetic
    @get:JvmSynthetic
    var orientation: Orientation by this.builder::orientation
}


fun PageBuilderApi.container(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply(block))
//fun DocumentBuilderApi.container(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply(block))
