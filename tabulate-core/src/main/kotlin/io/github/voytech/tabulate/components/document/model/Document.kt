package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.components.document.template.DocumentTemplate
import io.github.voytech.tabulate.components.document.template.DocumentTemplateContext
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.model.ModelWithAttributes

class Document internal constructor(
    @get:JvmSynthetic
    internal val nodes: List<AbstractModel<*,*,*>>,
    @get:JvmSynthetic override val attributes: Attributes?,
    override val id: String
) : ModelWithAttributes<DocumentTemplate,Document,DocumentTemplateContext>()