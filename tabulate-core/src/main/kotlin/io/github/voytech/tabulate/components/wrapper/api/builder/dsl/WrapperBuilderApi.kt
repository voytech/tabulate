package io.github.voytech.tabulate.components.wrapper.api.builder.dsl

import io.github.voytech.tabulate.components.wrapper.api.builder.WrapperBuilderState
import io.github.voytech.tabulate.components.wrapper.model.Wrapper
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import java.util.*


/**
 * Kotlin type-safe DSL document sheet builder API for defining entire document sheet.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class WrapperBuilderApi internal constructor() :
    CompositeModelBuilderApi<Wrapper, WrapperBuilderState>(WrapperBuilderState()) {

    @get:JvmSynthetic
    @set:JvmSynthetic
    var id: String by this.builder::id

    @JvmSynthetic
    fun attributes(block: WrapperAttributesBuilderApi.() -> Unit) {
        WrapperAttributesBuilderApi(builder).apply(block)
    }
}

@TabulateMarker
class WrapperAttributesBuilderApi internal constructor(private val builderState: WrapperBuilderState) {

    @JvmSynthetic
    fun attribute(attribute: AttributeBuilder<*>) {
        builderState.attribute(attribute)
    }

    val center: DSLCommand get() {
        builderState.attribute(AlignmentAttribute.Builder(Wrapper::class.java).apply { center })
        return DSLCommand
    }
    val left: DSLCommand get() {
        builderState.attribute(AlignmentAttribute.Builder(Wrapper::class.java).apply { left })
        return DSLCommand
    }
    val right: DSLCommand get() {
        builderState.attribute(AlignmentAttribute.Builder(Wrapper::class.java).apply { right })
        return DSLCommand
    }
    val middle: DSLCommand get() {
        builderState.attribute(AlignmentAttribute.Builder(Wrapper::class.java).apply { middle })
        return DSLCommand
    }
    val top: DSLCommand get() {
        builderState.attribute(AlignmentAttribute.Builder(Wrapper::class.java).apply { top })
        return DSLCommand
    }
    val bottom: DSLCommand get() {
        builderState.attribute(AlignmentAttribute.Builder(Wrapper::class.java).apply { bottom })
        return DSLCommand
    }

    val fullSize: DSLCommand get() {
        fullHeight
        fullWidth
        return DSLCommand
    }

    val fullWidth: DSLCommand get() {
        builderState.attribute(WidthAttribute.builder(Wrapper::class.java).apply {  100.percents() })
        return DSLCommand
    }

    val halfWidth: DSLCommand get() {
        builderState.attribute(WidthAttribute.builder(Wrapper::class.java).apply {  50.percents() })
        return DSLCommand
    }
    val width25: DSLCommand get() {
        builderState.attribute(WidthAttribute.builder(Wrapper::class.java).apply {  25.percents() })
        return DSLCommand
    }

    val width50: DSLCommand get() {
        builderState.attribute(WidthAttribute.builder(Wrapper::class.java).apply {  50.percents() })
        return DSLCommand
    }

    val width75: DSLCommand get() {
        builderState.attribute(WidthAttribute.builder(Wrapper::class.java).apply {  75.percents() })
        return DSLCommand
    }

    val fullHeight: DSLCommand get() {
        builderState.attribute(HeightAttribute.builder(Wrapper::class.java).apply { 100.percents() })
        return DSLCommand
    }

    val halfHeight: DSLCommand get() {
        builderState.attribute(HeightAttribute.builder(Wrapper::class.java).apply { 50.percents() })
        return DSLCommand
    }

    val height25: DSLCommand get() {
        builderState.attribute(HeightAttribute.builder(Wrapper::class.java).apply {  25.percents() })
        return DSLCommand
    }

    val height50: DSLCommand get() {
        builderState.attribute(HeightAttribute.builder(Wrapper::class.java).apply {  50.percents() })
        return DSLCommand
    }
}

fun CompositeModelBuilderApi<*,*>.align(block: WrapperAttributesBuilderApi.() -> Unit): WrapperBuilderApi = WrapperBuilderApi().also {
    bind(it.apply {
        attributes(block)
    })
}
