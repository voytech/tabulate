package io.github.voytech.tabulate.components.text.template

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextContext
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.ExportTemplateServices
import io.github.voytech.tabulate.core.template.TemplateContext


class TextTemplate : ExportTemplate<TextTemplate, Text, TextTemplateContext>() {

    override fun doExport(templateContext: TextTemplateContext) = with(templateContext.services) {
        with(templateContext.model) {
            render(TextContext(value))
        }
    }

    override fun createTemplateContext(parentContext: TemplateContext<*, *>, model: Text): TextTemplateContext =
        TextTemplateContext(model, parentContext.services)
}

class TextTemplateContext(model: Text, services: ExportTemplateServices) :
    TemplateContext<TextTemplateContext, Text>(model, mutableMapOf(), services)