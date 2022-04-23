package io.github.voytech.tabulate.components.sheet.api.builder

import io.github.voytech.tabulate.components.sheet.model.Sheet
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.Model


/**
 * Top level builder state for creating sheet model.
 * Manages mutable state that is eventually materialized to sheet model.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class SheetBuilderState : CompositeModelBuilderState<Sheet> {

    @get:JvmSynthetic
    internal var node: ModelBuilderState<*>? = null

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var name: String = "untitled sheet"

    @JvmSynthetic
    override fun <E: Model> bind(node: ModelBuilderState<E>) {
        this.node = node
    }

    @JvmSynthetic
    override fun build(): Sheet = Sheet(
        node = node?.build(),
        id = name
    )

}
