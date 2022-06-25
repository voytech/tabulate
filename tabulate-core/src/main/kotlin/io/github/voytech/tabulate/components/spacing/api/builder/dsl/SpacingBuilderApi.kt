package io.github.voytech.tabulate.components.spacing.api.builder.dsl

import io.github.voytech.tabulate.components.spacing.api.builder.SpacingBuilderState
import io.github.voytech.tabulate.components.spacing.model.Spacing
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.MeasuredValue
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width

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
    private var width: Width by builder::width

    @set:JvmSynthetic
    @get:JvmSynthetic
    private var height: Height by builder::height

    fun Number.pt(): MeasuredValue = MeasuredValue(toFloat(), UnitsOfMeasure.PT)
    fun Number.px(): MeasuredValue = MeasuredValue(toFloat(), UnitsOfMeasure.PX)

    fun width(block: () -> MeasuredValue) {
        width = block().width()
    }

    fun height(block: () -> MeasuredValue) {
        height = block().height()
    }
}

fun CompositeModelBuilderApi<*,*>.space(block: SpacingBuilderApi.() -> Unit) = bind(SpacingBuilderApi().apply(block))
