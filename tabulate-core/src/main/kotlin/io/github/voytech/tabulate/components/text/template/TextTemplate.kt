package io.github.voytech.tabulate.components.text.template

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.model.SomeSize
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.model.orEmpty
import io.github.voytech.tabulate.core.template.*


class TextTemplate : ExportTemplate<TextTemplate, Text, TextTemplateContext>() {

    override fun doExport(templateContext: TextTemplateContext) = with(templateContext) {
        createLayoutScope {
            render(TextRenderable(getTextValue(), model.attributes.orEmpty().forContext<TextRenderable>()))
        }
    }

    private fun TextTemplateContext.getTextValue(): String = model.valueSupplier?.let { value(it) } ?: model.value

    override fun computeSize(parentContext: TemplateContext<*, *>, model: Text): SomeSize = SomeSize(
        model.attributes?.get<WidthAttribute>()?.value,
        model.attributes?.get<HeightAttribute>()?.value,
    )

    override fun createTemplateContext(parentContext: TemplateContext<*, *>, model: Text): TextTemplateContext =
        TextTemplateContext(model, parentContext.stateAttributes, parentContext.services)

}

class TextTemplateContext(
    model: Text,
    stateAttributes: MutableMap<String, Any>,
    services: ExportTemplateServices,
) : TemplateContext<TextTemplateContext, Text>(model, stateAttributes, services)