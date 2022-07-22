package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.model.ModelWithAttributes

class Document internal constructor(
    @get:JvmSynthetic
    internal val id: String,
    @get:JvmSynthetic
    internal val nodes: List<Model<*>>,
    @get:JvmSynthetic override val attributes: Attributes?,
) : ModelWithAttributes<Document>() {
    override fun getId(): String = id
}