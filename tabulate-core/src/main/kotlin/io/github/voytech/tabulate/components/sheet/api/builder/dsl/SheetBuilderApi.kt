package io.github.voytech.tabulate.components.sheet.api.builder.dsl

import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.sheet.api.builder.SheetBuilderState
import io.github.voytech.tabulate.components.sheet.model.Sheet
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

/**
 * Kotlin type-safe DSL document sheet builder API for defining entire document sheet.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class SheetBuilderApi internal constructor(): CompositeModelBuilderApi<Sheet, SheetBuilderState>(SheetBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var name: String by this.builder::name

}

fun DocumentBuilderApi.sheet(block: SheetBuilderApi.() -> Unit) = bind(SheetBuilderApi().apply(block))
