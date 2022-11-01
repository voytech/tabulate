package io.github.voytech.tabulate.components.text.model

import io.github.voytech.tabulate.components.text.template.TextTemplate
import io.github.voytech.tabulate.components.text.template.TextTemplateContext
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.ExecutionContext
import io.github.voytech.tabulate.core.model.ModelWithAttributes
import io.github.voytech.tabulate.core.model.ReifiedValueSupplier

class Text(
    @get:JvmSynthetic
    internal val value: String = "blank",
    internal val valueSupplier: ReifiedValueSupplier<*,String>?,
    override val attributes: Attributes?
): ModelWithAttributes<TextTemplate, Text, TextTemplateContext>() {

    override fun getExportTemplate() = TextTemplate()
}