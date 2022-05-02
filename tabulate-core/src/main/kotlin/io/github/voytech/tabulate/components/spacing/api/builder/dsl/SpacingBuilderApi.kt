package io.github.voytech.tabulate.components.spacing.api.builder.dsl

import io.github.voytech.tabulate.components.spacing.api.builder.SpacingBuilderState
import io.github.voytech.tabulate.components.spacing.model.Spacing
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

/**
 * Kotlin type-safe DSL document sheet builder API for defining entire document sheet.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class SpacingBuilderApi internal constructor(): CompositeModelBuilderApi<Spacing, SpacingBuilderState>(SpacingBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var widthInPoints: Float by builder::widthInPoints

    @set:JvmSynthetic
    @get:JvmSynthetic
    var heightInPoints: Float by builder::heightInPoints
}

fun CompositeModelBuilderApi<*,*>.space(block: SpacingBuilderApi.() -> Unit) = bind(SpacingBuilderApi().apply(block))
