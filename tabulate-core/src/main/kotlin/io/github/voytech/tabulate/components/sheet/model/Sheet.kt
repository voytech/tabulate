package io.github.voytech.tabulate.components.sheet.model

import io.github.voytech.tabulate.components.sheet.template.SheetTemplate
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeConstraint
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.TemplateContext

class Sheet internal constructor(
    @get:JvmSynthetic
    internal val id: String,
    @get:JvmSynthetic
    internal val nodes: List<Model<*>>? = null,
) : Model<Sheet> {
    override fun getId(): String = id

    override fun getExportTemplate(): ExportTemplate<Sheet, out TemplateContext<Sheet>> = SheetTemplate()
}
