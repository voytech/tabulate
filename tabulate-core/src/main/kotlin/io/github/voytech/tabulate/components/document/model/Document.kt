package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.Model

class Document internal constructor(
    @get:JvmSynthetic
    internal val id: String,
    @get:JvmSynthetic
    internal val nodes: List<Model>,
    @get:JvmSynthetic
    internal val attributes: Attributes<*>?,
) : Model {
    override fun getId(): String = id

    override fun getExplicitAttributeCategories(): Set<Class<out Attribute<*>>> {
        TODO("Not yet implemented")
    }
}