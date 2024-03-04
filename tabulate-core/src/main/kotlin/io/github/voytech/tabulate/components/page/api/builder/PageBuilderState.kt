package io.github.voytech.tabulate.components.page.api.builder

import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.AbstractModel


/**
 * Top level builder state for creating sheet model.
 * Manages mutable state that is eventually materialized to sheet model.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class PageBuilderState : CompositeModelBuilderState<Page> {

    @get:JvmSynthetic
    internal var nodes: MutableList<ModelBuilderState<*>> = mutableListOf()

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var name: String = "untitled"

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var header: ModelBuilderState<*>? = null

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var footer: ModelBuilderState<*>? = null

    @JvmSynthetic
    override fun <E : AbstractModel> bind(node: ModelBuilderState<E>) {
        nodes.add(node)
    }

    @JvmSynthetic
    override fun build(): Page = Page(
        name = name,
        models = nodes.map { it.build() },
        header = header?.build(),
        footer = footer?.build()
    )

}
