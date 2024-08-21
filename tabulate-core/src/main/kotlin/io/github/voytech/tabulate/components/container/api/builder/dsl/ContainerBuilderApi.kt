package io.github.voytech.tabulate.components.container.api.builder.dsl

import io.github.voytech.tabulate.components.container.api.builder.ContainerBuilderState
import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.WrapperBuilderApi
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.DescendantsIterationsKind
import io.github.voytech.tabulate.core.model.Orientation


/**
 * Kotlin type-safe DSL document sheet builder API for defining entire document sheet.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class ContainerBuilderApi internal constructor() :
    CompositeModelBuilderApi<Container, ContainerBuilderState>(ContainerBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var id: String by this.builder::id

    @set:JvmSynthetic
    @get:JvmSynthetic
    var orientation: Orientation by this.builder::orientation

    val forcePreMeasure: DSLCommand
        get() {
            this.builder.forcePreMeasure = true; return DSLCommand
        }

    val immediateIterations: DSLCommand
        get() {
            this.builder.descendantsIterationsKind = DescendantsIterationsKind.IMMEDIATE; return DSLCommand
        }

    val postponedIterations: DSLCommand
        get() {
            this.builder.descendantsIterationsKind = DescendantsIterationsKind.POSTPONED; return DSLCommand
        }

    @JvmSynthetic
    fun attributes(block: ContainerAttributesBuilderApi.() -> Unit) {
        ContainerAttributesBuilderApi(builder).apply(block)
    }
}

@TabulateMarker
class ContainerAttributesBuilderApi internal constructor(private val builderState: ContainerBuilderState) {
    @JvmSynthetic
    fun attribute(attribute: AttributeBuilder<*>) {
        builderState.attribute(attribute)
    }
}

//TODO introduce api marker on which API builders below can be installed in one shot

fun PageBuilderApi.content(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply(block))

fun PageBuilderApi.vertical(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply {
    block()
    orientation = Orientation.VERTICAL
})

infix fun WrapperBuilderApi.vertical(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply {
    block()
    orientation = Orientation.VERTICAL
})

infix fun WrapperBuilderApi.horizontal(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply {
    block()
    orientation = Orientation.HORIZONTAL
})

fun PageBuilderApi.horizontal(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply {
    block()
    orientation = Orientation.HORIZONTAL
})

fun ContainerBuilderApi.content(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply(block))

infix fun WrapperBuilderApi.content(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply(block))

fun ContainerBuilderApi.vertical(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply {
    block()
    orientation = Orientation.VERTICAL
})

fun ContainerBuilderApi.horizontal(block: ContainerBuilderApi.() -> Unit) = bind(ContainerBuilderApi().apply {
    block()
    orientation = Orientation.HORIZONTAL
})