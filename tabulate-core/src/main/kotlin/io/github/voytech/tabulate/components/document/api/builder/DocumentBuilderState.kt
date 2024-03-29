package io.github.voytech.tabulate.components.document.api.builder

import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.api.builder.AttributesAwareBuilder
import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.reify


/**
 * Top level builder state for creating document model.
 * Manages mutable state that is eventually materialized to document model.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class DocumentBuilderState : CompositeModelBuilderState<Document>, AttributesAwareBuilder<Document>() {

    @get:JvmSynthetic
    internal val nodeBuilders: MutableList<ModelBuilderState<*>> = mutableListOf()

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var name: String = "untitled document"

    @JvmSynthetic
    override fun <E : AbstractModel> bind(node: ModelBuilderState<E>) {
        nodeBuilders.add(node)
    }

    @JvmSynthetic
    override fun build(): Document = Document(
        attributes = attributes(),
        models = nodeBuilders.map { it.build() },
        id = name
    )

    override fun modelClass(): Class<Document> = reify()

}
