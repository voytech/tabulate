package io.github.voytech.tabulate.components.page.api.builder

import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.core.api.builder.BuiltModel
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState


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

    @JvmSynthetic
    override fun <E : BuiltModel<E>> bind(node: ModelBuilderState<E>) {
        nodes.add(node)
    }

    @JvmSynthetic
    override fun build(): Page = Page(
        name = name,
        nodes = nodes.map { it.build() },
    )

}
