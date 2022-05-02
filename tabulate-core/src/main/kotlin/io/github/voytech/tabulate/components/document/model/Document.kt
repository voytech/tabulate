package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.Model

class Document internal constructor(
    @get:JvmSynthetic
    internal val id: String,
    @get:JvmSynthetic
    internal val nodes: List<Model<*>>,
    @get:JvmSynthetic
    internal val attributes: Attributes<*>?,
) : Model<Document> {
    override fun getId(): String = id

}