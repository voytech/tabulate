package io.github.voytech.tabulate.components.margins.api.builder.dsl

import io.github.voytech.tabulate.components.margins.api.builder.MarginsBuilderState
import io.github.voytech.tabulate.components.margins.model.Margins
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
class MarginsBuilderApi internal constructor(): CompositeModelBuilderApi<Margins, MarginsBuilderState>(MarginsBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    private var width: Width by builder::width

    @set:JvmSynthetic
    @get:JvmSynthetic
    private var height: Height by builder::height

    fun Number.pt(): MeasuredValue = MeasuredValue(toFloat(), UnitsOfMeasure.PT)
    fun Number.px(): MeasuredValue = MeasuredValue(toFloat(), UnitsOfMeasure.PX)

    fun left(block: () -> MeasuredValue) {
        width = block().width()
    }

    fun top(block: () -> MeasuredValue) {
        height = block().height()
    }
}

fun CompositeModelBuilderApi<*,*>.margins(block: MarginsBuilderApi.() -> Unit) = bind(MarginsBuilderApi().apply(block))
