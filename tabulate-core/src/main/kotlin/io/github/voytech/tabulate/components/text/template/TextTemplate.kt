package io.github.voytech.tabulate.components.text.template

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextRenderable.Companion.Text
import io.github.voytech.tabulate.core.model.Size
import io.github.voytech.tabulate.core.model.SomeSize
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.ExportTemplateServices
import io.github.voytech.tabulate.core.template.TemplateContext


class TextTemplate : ExportTemplate<TextTemplate, Text, TextTemplateContext>() {

    override fun doExport(templateContext: TextTemplateContext) = with(templateContext) {
        createLayoutScope {
            render(Text())
        }
    }

    override fun computeSize(parentContext: TemplateContext<*, *>, model: Text): SomeSize = SomeSize(
        model.attributes?.get<WidthAttribute>()?.value,
        model.attributes?.get<HeightAttribute>()?.value,
    )

    override fun createTemplateContext(parentContext: TemplateContext<*, *>, model: Text): TextTemplateContext =
        TextTemplateContext(model, parentContext.services)

}

class TextTemplateContext(model: Text, services: ExportTemplateServices) :
    TemplateContext<TextTemplateContext, Text>(model, mutableMapOf(), services)