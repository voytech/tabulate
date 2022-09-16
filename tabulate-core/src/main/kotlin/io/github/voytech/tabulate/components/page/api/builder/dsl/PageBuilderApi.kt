package io.github.voytech.tabulate.components.page.api.builder.dsl

import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.PageBuilderState
import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.core.api.builder.BuiltModel
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.ModelBuilderApi
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

    fun header(block: HeaderBuilderApi.() -> Unit) {
        HeaderBuilderApi(this).apply(block)
    }

    fun footer(block: FooterBuilderApi.() -> Unit) {
        FooterBuilderApi(this).apply(block)
    }

}

@TabulateMarker
class HeaderBuilderApi internal constructor(private val api: PageBuilderApi) {
    fun <E : BuiltModel<E>, R : ModelBuilderApi<E, *>> bind(other: R) {
        api.builder.header = other.builder
    }
}

@TabulateMarker
class FooterBuilderApi internal constructor(private val api: PageBuilderApi) {
    fun <E : BuiltModel<E>, R : ModelBuilderApi<E, *>> bind(other: R) {
        api.builder.footer = other.builder
    }
}


fun DocumentBuilderApi.page(block: PageBuilderApi.() -> Unit) = bind(PageBuilderApi().apply(block))
fun DocumentBuilderApi.sheet(block: PageBuilderApi.() -> Unit) = page(block)
