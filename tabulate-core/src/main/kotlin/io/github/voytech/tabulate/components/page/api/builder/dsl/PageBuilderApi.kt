package io.github.voytech.tabulate.components.page.api.builder.dsl

import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.PageBuilderState
import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

/**
 * Kotlin type-safe DSL document sheet builder API for defining entire document sheet.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class PageBuilderApi internal constructor(): CompositeModelBuilderApi<Page, PageBuilderState>(PageBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var name: String by this.builder::name

}

fun DocumentBuilderApi.page(block: PageBuilderApi.() -> Unit) = bind(PageBuilderApi().apply(block))
fun DocumentBuilderApi.sheet(block: PageBuilderApi.() -> Unit) = page(block)
